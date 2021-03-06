import React from 'react';
import WebSocketService from '../webSocket/WebSocketService';
import WebSocketSubscriptionManager from '../webSocket/WebSocketSubscriptionManager';
import Chat from '../common/Chat';

export default React.createClass({

  componentDidMount() {
    const gameId = this.props.params.gameId;
    const subscriptions = [];
    subscriptions.push({location: `/topic/chat/game/${gameId}/user`, subscription: displayChat.bind(this)});
    subscriptions.push({location: `/topic/chat/game/${gameId}/system`, subscription: displayChat.bind(this)});
    WebSocketSubscriptionManager.subscribe(this, subscriptions);
  },

  componentWillUnmount() {
    WebSocketSubscriptionManager.unsubscribe(this);
  },

  render() {
    return (
      <div>
        <p>{this.props.params.gameId}</p>
        <Chat ref="gameChat" sendChat={sendChat.bind(this, this.props.params.gameId)} />
      </div>
    )
  }

})

function displayChat(message) {
  this.refs.gameChat.displayChat(message.body);
}

function sendChat(gameId, message) {
  const gameMessage = {
    message: message,
    receiverUsernames: null,
    gameId: gameId,
    tableId: null
  };

  WebSocketService.send('/app/sendchatmessage', gameMessage);
}
