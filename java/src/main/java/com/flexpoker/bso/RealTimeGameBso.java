package com.flexpoker.bso;

import com.flexpoker.model.Game;
import com.flexpoker.model.RealTimeGame;

public interface RealTimeGameBso {

    RealTimeGame get(Game game);

    RealTimeGame put(Game game, RealTimeGame realTimeGame);

}
