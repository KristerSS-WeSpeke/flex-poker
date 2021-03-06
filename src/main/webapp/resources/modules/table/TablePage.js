import React from 'react';
import WebSocketService from '../webSocket/WebSocketService';
import WebSocketSubscriptionManager from '../webSocket/WebSocketSubscriptionManager';
import cardData from './cardData';
import CommonCards from './CommonCards';
import MyCards from './MyCards';
import Seat from './Seat';
import PokerActions from './PokerActions';
import Chat from '../common/Chat';
import _ from 'lodash';

export default React.createClass({

  getInitialState() {
    return {
      myLeftCard: null,
      myRightCard: null,
      totalPot: 0,
      visibleCommonCards: [],
      seats: [],
      tableVersion: 0
    }
  },

  componentDidMount() {
    const gameId = this.props.params.gameId;
    const tableId = this.props.params.tableId;

    const subscriptions = [];
    subscriptions.push({location: `/topic/game/${gameId}/table/${tableId}`, subscription: receiveTableUpdate.bind(this)});
    subscriptions.push({location: `/topic/chat/game/${gameId}/table/${tableId}/user`, subscription: displayChat.bind(this)});
    subscriptions.push({location: `/topic/chat/game/${gameId}/table/${tableId}/system`, subscription: displayChat.bind(this)});

    WebSocketSubscriptionManager.subscribe(this, subscriptions);

    document.addEventListener(`pocketCardsReceived-${tableId}`, evt => {
      this.setState({
        myLeftCard: cardData[evt.detail.cardId1],
        myRightCard: cardData[evt.detail.cardId2]
      })
    });

  },

  componentWillUnmount() {
    WebSocketSubscriptionManager.unsubscribe(this);
  },

  render() {
    const username = window.username;
    const mySeat = this.state.seats.find(seat => seat.name === username);

    return (
      <div>
        <p>Game Id: {this.props.params.gameId}</p>
        <p>Table Id: {this.props.params.tableId}</p>
        <p>Version: {this.state.tableVersion}</p>

        <div className={"poker-table"}>
          <div>{this.state.totalPot}</div>
          <CommonCards visibleCommonCards={this.state.visibleCommonCards} />
          <div className={"seat-holder"}>
            {
              this.state.seats.map((seat, index) =>
                <Seat seat={seat} mySeat={seat === mySeat} key={index} />
              )
            }
          </div>
        </div>

        <MyCards myLeftCard={this.state.myLeftCard} myRightCard={this.state.myRightCard} />
        {
          _.isNil(mySeat)
            ? null
            : <PokerActions
                gameId={this.props.params.gameId}
                tableId={this.props.params.tableId}
                actionOn={mySeat.actionOn}
                callAmount={mySeat.callAmount}
                raiseTo={mySeat.raiseTo} />
        }

        <Chat ref="tableChat" sendChat={sendChat.bind(this, this.props.params.gameId, this.props.params.tableId)} />
      </div>
    )
  }

})

function displayChat(message) {
  this.refs.tableChat.displayChat(message.body);
}

function sendChat(gameId, tableId, message) {
  const tableMessage = {
    message: message,
    receiverUsernames: null,
    gameId: gameId,
    tableId: tableId
  };

  WebSocketService.send('/app/sendchatmessage', tableMessage);
}

function receiveTableUpdate(message) {
  let table = JSON.parse(message.body);

  if (table.version > this.state.tableVersion) {
    this.setState({
      totalPot: table.totalPot,
      visibleCommonCards: table.visibleCommonCards,
      seats: table.seats,
      tableVersion: table.version
    });
  }
}
