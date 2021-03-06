import React from 'react';
import { Button } from 'react-bootstrap';

export default ({gameList, gameOpenedCallback}) => {
  return (
    <div className={'game-list'}>
      <table className={'table table-striped table-bordered table-hover'}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Stage</th>
            <th>Registered Players</th>
            <th>Total Players</th>
            <th>Players Per Table</th>
            <th>Creator</th>
            <th>Created</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {
            gameList.map((game, index) => {
              return (
                <tr key={index}>
                  <td>{game.name}</td>
                  <td>{game.stage}</td>
                  <td>{game.numberOfRegisteredPlayers}</td>
                  <td>{game.maxNumberOfPlayers}</td>
                  <td>{game.maxPlayersPerTable}</td>
                  <td>{game.createdBy}</td>
                  <td>{game.createdOn}</td>
                  <td>
                    <Button onClick={() => gameOpenedCallback(game.id)} disabled={game.stage !== 'REGISTERING'}>
                      Open Game
                    </Button>
                  </td>
                </tr>
              )
            })
          }
        </tbody>
      </table>
    </div>
  )
}
