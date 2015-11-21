package com.jbooktrader.platform.portfolio.manager;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.trader.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * @author Eugene Kononov
 */

public class PortfolioManager {
    private final int positionSizePerStrategy;
    private final int maxOpenPositions;
    private final TraderAssistant traderAssistant;
    private final Dispatcher dispatcher;

    public PortfolioManager() {
        traderAssistant = Dispatcher.getInstance().getTrader().getAssistant();
        dispatcher = Dispatcher.getInstance();
        PreferencesHolder prefs = PreferencesHolder.getInstance();
        positionSizePerStrategy = Integer.parseInt(prefs.get(PositionSizePerStrategy));
        maxOpenPositions = Integer.parseInt(prefs.get(MaxOpenPositions));
    }

    public int getSize(Strategy strategy) {
        Mode mode = dispatcher.getMode();
        if (mode == Mode.BackTest || mode == Mode.Optimization) {
            return 1;
        }

        int currentPosition = strategy.getPositionManager().getCurrentPosition();
        if (currentPosition != 0) {
            return positionSizePerStrategy;
        }

        int openPositions = 0;
        for (Strategy s : traderAssistant.getAllStrategies()) {
            int position = s.getPositionManager().getCurrentPosition();
            if (position != 0) {
                openPositions++;
            }
        }

        if (openPositions >= maxOpenPositions) {
            strategy.goFlat();
            String msg = "Strategy " + strategy.getName() + " is not allowed to enter a position because ";
            msg += maxOpenPositions + " other positions are currently open.";
            dispatcher.getEventReport().report("PortfolioManager", msg);
            return 0;
        }

        return positionSizePerStrategy;
    }
}
