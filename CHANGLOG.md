
9.01 (July 8, 2013)
-------------------

* Email/SMS notifications
* Third party library updated: IB API from 9.63 to 9.66, JFreeChart from 1.0.13 to 1.0.14, JCommon from 1.0.16 to 1.0.17, JCalendar from 1.3.3 to 1.4
* Indicator initialization improved
* Fixed compilation time warnings related to generics
* Added a build script for building JBookTrader in an OS-independent and IDE-independent way
* Backtester and optimizer cache the data sets for fast loading
* Ability to run JBookTrader from a single JAR
* Ability to switch to "forward test" mode and to suspend live trading
* New performance metric: CPI
* Portfolio Manager for dynamic position sizing
* Fixed failing unit tests
* Rejected orders are handled correctly
* Ability to trade an FA account
* Calculation of "most liquid contract" for the CL
* Very efficient calculation if min/max in a moving window, as well as standard deviation
* Ability to force-close the positions if the connectivity gap is wide enough
* Exchange hours detection
* Fixed thread safety issues in optimizer
* Fixed average duration calculation
* Prevent entering the position when less than 15 minutes left in the trading interval
* Rearranged the packages
* Max single loss calculations


8.07 (January 9, 2012)
----------------------

* Improved the methodology of indicator initialization
* Fixed date scroll issues in the charts
* Optimization results are now sortable by table column
* Fixed the problem associated with the 2104 error code


8.06 (April 24, 2011)
---------------------

* Strategy information dialog uses streaming book information
* Trades are annotated slightly differently on the performance chart


8.05 (March 26, 2011)
---------------------

* Fixed the "cancel" functionality for the optimization and backtesting dialogs
* Removed the C2 feature
* Fixed a problem preventing two instances of the same indicator being shown on the chart
* Added an "inclusion" criteria for the optimization dialog


8.04 (February 27, 2011)
------------------------

* Revised Tension indicator
* Changed North America bundled future commissions from $2.00 to $2.01
* Strategy report is now more readable
* Revised sample strategies


8.03 (February 12, 2011)
------------------------

* Refactored various pieces for clarity and performance


8.02 (November 24, 2010)
------------------------

* Both the "brute force" and the "divide-and-conquer" optimizers run about 2 times faster


8.01 (November 14, 2010)
------------------------

* Added support for volume
* Added new indicators
* Fixed and refactored MovingWindow? classes
* Added a new "bias" metric


7.10 (October 29, 2010)
-----------------------

* Added 3 new indicators, including Kalman filter
* Added the "settlement time" for better backtesting
* Both "brute force" and "divide-and-conquer" optimizers run about 1.3 to 2.0 times faster
* Improved the MovingWindow? class
* Fixed a minor problem with a unicode character


7.09 (October 11, 2010)
-----------------------

* Refactored several classes
* Fixed NTP clock timeouts and shifts
* Added the "Average Duration" performance metric


7.08 (October 6, 2010)
----------------------

* Fixed the issue of leaving open positions overnight
* Fixed US futures commissions
* Fixed market depth resetting issues
* Improved D&C optimizer
* Improved web interface


7.07 (September 8, 2010)
------------------------

* Set look and feel to Nimbus and removed other LAFs
* Simplified web interface
* Reorganized indicators
* Simplified backtesting dialog
* Improved reporting
* Simplified performance charts
* Fixed a recursive call issue with NTP clock
* Fixed the progress indicator issue in divide-and-conquer dialog
* Reorganized sample strategies


7.06 (June 12, 2010)
--------------------

* Upgraded from Substance 5.2 to 6.0
* Added three more types of look and feel: Liquid, SeaGlass?, and Nimbus
* Simplified Windows start-up script
* Fixed memory leak in back tester
* Added two more sample strategies


7.05 (May 31, 2010)
-------------------

* Added new indicators
* Improved performance of certain indicators, such as Bollinger, PriceSMA, and Price Volatility
* Added a bar size option to the back tester dialog
* Upgraded IB API to version 9.63
* Added new strategies
* Added new data set for back testing


7.04 (September 6, 2009)
------------------------

* Fixed an NTP clock issue which caused "time shifts"
* Reports use NTP clock
* Reports format and appearance revised
* IB error 322 is handled


7.03 (August 24, 2009)
----------------------

* 1-second market depth representation changed
* JBT is driven by atomic clock
* Added about 15 indicators
* Collective2 code restructured
* Fixed some issues in web admin console
* Optimizers are faster
* Fixed a concurrency issue in optimization map
* Fixed order processing issue which caused the "Order would cross related resting order" message and order cancellation
* Added sample strategies


7.02 (July 28, 2009)
--------------------

* Revised and improved the "divide and conquer" optimizer
* Both "brute force" and "divide and conquer" optimizers are faster
* Web console is more dynamic
* Range of dates can be specified in the backtest and optimization dialogs
* Fixed scroll bar and zooming issues in performance chart
* Added "user manual" and "release notes" to main menu
* Market depth validation is stricter and more consistent
* Better look and feel with the substance L&F
* User manual updated


7.01 (July 13, 2009)
--------------------

* Fixed the "market depth reset" bug which caused the market data "freezing"
* Replaced the "liquid" L&F with the "substance" L&F
* Functionality and appearance of web console significantly improved
* Updated user manual
* Added a new manual, describing how to set up JBT in Eclipse on Mac OS
* Minor GUI tweaks for Mac OS
* Optimizer window "remembers" its last size and position
* Updated sample strategies


6.11 (July 7, 2009)
-------------------

* Removed "email notification" feature
* Upgraded jcommons from 1.0.15 to 1.0.16 and jfreechart from 1.0.12 to 1.0.13
* Updated user manual
* Merged the "bid size" and the "ask size" columns into a single "market depth column" in the strategy table
* Minor changes to UI


6.10 (July 3, 2009)
-------------------

* Fixed validation for the 10-level deep book
* Upgraded to IB API v9.62
* Added cumulative bid and cumulative size columns to main table
* Updated sample strategies
* Minor refactoring


6.09 (June 26, 2009)
--------------------

* Fixed a bug in resetting market depth (reported by shaggsthestud)


6.08 (March 27, 2009)
---------------------

* Upgraded third party libraries: ibapi from 9.51 to 9.6, jcommon from 1.0.14 to 1.0.15, and jfreechart from 1.0.11 to 1.0.12
* Better handling of disconnect/reconnect events, order placement, and executions
* Market depth detected and processed up to level 10
* Fixed a problem in the "do not log/report/send duplicate messages" logic


6.07 (March 21, 2009)
---------------------

* Fixed the reset() method for some indicators
* Added another column to the main table
* Fixed a resource leak in optimizer (reported by Crichton)
* Fixed a problem in class finder (reported by Sonny)
* Avoid reporting (and emailing) the same error multiple times (reported by Javier)
* Adjusted Performance Index (PI) calculations so that they are the same as those for System Quality Number (SQN)
* Moved the start of ES trading from 9:35 to 10:00, so that the indicators have enough time to settle
* Revised sample strategies


6.06 (January 22, 2009)
-----------------------

* Fixed a compilation problem with some of the sample strategies.
* Fixed a problem where JBT could not distinguish between two different Forex contracts with the same symbol (such as EUR.USD and EUR.GBP)
* Minor refactoring of exception handling


6.05 (January 18, 2009)
-----------------------

* Fixed a problem in the "divide-and-conquer" optimizer which caused the optimizer to wander off course. The D&C optimizer is much more likely to find the peaks.
* Fixed a problem which caused the optimization results to be truncated. The optimization maps now preserve all the data, which makes reading the maps more intuitive


6.04 (January 9, 2009)
----------------------

* Improved the the "divide-and-conquer" optimizer
* Added two new sample strategies
* Refactored various parts of the code


6.03 (November 18, 2008)
------------------------

* Improved the indicator framework
* Better support for backtesting and optimization of large data files
* Bid/ask spread is set by the strategy, instead of setting it in the historical data file
* Removed manual saving of market depth data
* Revised market book implementation
* Added sample strategies


6.02 (October 21, 2008)
-----------------------

* Simplified historical data format
* Fixed Collective2 problems
* Indicator values reset at the beginning of each day during backtesting and optimization
* Market book logic refactored
* Advisor accounts are no longer supported
* Non-HTML reports are no longer supported
* E-Mail sender simplified
* Updated IB API, JFreechart, and JCommons to their latest releases
* Replaced Jetty web server with Sun web server


6.01 (October 11, 2008)
-----------------------

* Indexes and volume are no longer recorded or used
* Command line interface is decommissioned
* Optimization maps improved
* PI calculation chaned
* Trade counting methodology changed
* Integration with Collective2
* Added JUnit coverage
* Market book validation improved


5.09 (September 27, 2008)
-------------------------

* Added new indicators
* Command line interface
* Web admin console to monitor JBT remotely
* Improved book validation
* Revised sample strategies


5.08 (September 12, 2008)
-------------------------

* Refined exception handling (thanks, Yueming)
* Changed PI, as suggested by Dyno and Kelvin: http://groups.google.com/group/jbooktrader/browse_thread/thread/f35707d6d1e5163f/7a54ce74a9a64fe6#7a54ce74a9a64fe6
* Main frame shows version number (thanks, Florent)
* Adjusted the "divide-and-conquer" optimizer
* Revised sample strategies


5.07 (September 6, 2008)
------------------------

* Optimizer is now multi-threaded and fully utilizes all available processors
* Fixed an issue of exceptions in the report when trading stops at the end of the day


5.06 (September 1, 2008)
------------------------

* TRIN and VIX are no longer used or recorded
* Revised exception handling
* Moved indicator logic out of Strategy class to IndicatorManager? class
* Removed unnecessary reporting
* Fixed scroll bar issues in performance chart
* Updated sample strategies


5.05 (August 29, 2008)
----------------------

* Fixed a bug in stock commission calculation: http://code.google.com/p/jbooktrader/issues/detail?id=11&can=1
* Added the "from" field to remote notification setup. This allows using email accounts other than GMail as SMTPS hosts
* Updated sample strategies


5.04 (August 26, 2008)
----------------------

* All strategies have automatic access to TICK, TRIN, and VIX
* Updated jcommon and jfreechart libraries to latest versions


5.03 (August 24, 2008)
----------------------

* Remote notification feature improved
* Minor fix in CME data converter
* All sample strategies have been simplified by extending a common base strategy
* Added two volume-based strategies


5.02 (August 23, 2008)
----------------------

* Optimized sample strategies on the IB data set (June 2 to August 22)


5.01 (August 19, 2008)
----------------------

* Historical data format changed. Each record now represents 1-second snapshot of the market and contains the following 7 fields: date, time, period's lowest book balance, period's highest book balance, best bid at the end of the period, best ask at the end of * the period, volume of traded contracts during the period.
* Max DD is calculated on closed trades
* Indicators are "self-named", i.e., no name is required when indicators are instantiated by strategies
* Strategy structure simplified: "market depth" is no longer needed to be passed to indicators
* The relationship between market book and strategies is "one-to-many". Specifically, if multiple strategies are trading the same instrument, only one market book will be created and shared by these strategies
* "Performance Index" metric has changed
* Non-trivial errors are emailed via the remote notification feature


4.05 (July 23, 2008)
--------------------

* Historical data format changed. Recorded depth balance is now "adjusted mean balance", as proposed by Dyno. Reference: http://groups.google.com/group/jbooktrader/browse_thread/thread/9df17cd7245b0225#. Details about this change: http://groups.google.com/group/* jbooktrader/browse_thread/thread/f41aabe4629a76c3#


4.04 (July 16, 2008)
--------------------

* Simplified historical data format
* Fixed memory leak when backtesting
* Improved performance of optimizers
* Revised sample strategies


4.03 (July 12, 2008)
--------------------

* Reorganized indicators
* Fixed Max DD calculations
* Heartbeat sent "on the minute"/"on the hour"
* Revised sample strategies


4.02 (July 02, 2008)
--------------------

* Added a new performance measure, called "exposure". It's a percentage of time the strategy was in the market (i.e, with either long or short position) during the test period
* Fixed a memory leak when repeatedly running a backtest
* Heartbeat email notifications are sent only during trading hours
* Revised indicators and sample strategies


4.01 (June 28, 2008)
--------------------

* Chart has a "bar size" control
* Optimizers don't have limits on the number of strategies
* Methodology of counting trades has changed
* Structure of strategies simplified
* Replaced "True Kelly" with "Kelly Criterion"


3.05 (June 23, 2008)
--------------------

* Optimization maps
* Both "brute force" and "divide and conquer" optimizers are faster
* Improved GUI
* Added new performance metric


3.04 (June 8, 2008)
-------------------

* Reorganized indicators
* Derivates of indicators can be created
* "Divide and Conquer" optimizer significantly improved
* JFreeChart package upgraded to latest version
* Remote notification can be sent to non-gmail accounts, such as cell phones
* Implemented "hearbeat" notifications


3.03 (June 1, 2008)
-------------------

* Changed file format for historical market depth data


3.02 (May 24, 2008)
-------------------

* Implemented converter from CME market depth format to JBT format (thanks, dyno)
* Converted CME data set is uploaded
* Main window title bar shows the running mode
* Max DD is updated on every price change, and not just at the trade time


3.01 (May 14, 2008)
-------------------

* Significant changes in coordination between market depth timer and strategy runner. The existing wait()/notify() mechanism caused missed notifications and subsequent discrepancies in multi-user tests. This mechanism has been replaced by direct invocations.
* Historical market depth file format has changed to capture the high/low balance for each burst
* Several GUI enhancements


2.13 (May 11, 2008)
-------------------

* Minor changes in timing of capture of market depths
* Market depths are recorded only when time is inside trading schedule


2.12 (May 03, 2008)
-------------------

* Market depth history is saved automatically
* Email notifications sent on disconnection and reconnection
* Added new indicator
* Market depth historical data format has changed slightly
* Revised sample trategies


2.11 (April 27, 2008)
---------------------

* Single thread used for market depth timing
* Added start up script for TWS


2.10 (April 21, 2008)
---------------------

* Adjusted timing of market depth capture
* Sample strategies revised
* Added more indicators
* Fixed a bug causing incorrect calculation of trade P&L


2.09 (April 13, 2008)
---------------------

* Changed timing of market depth capture
* Sample strategies revised


2.08 (April 07, 2008)
---------------------

* Fixed a problem with duplicate market depths


2.07 (April 06, 2008)
---------------------

* Revised the format for market depth historical data files
* Implemented a new methodology for capturing market depths in their "completed" state


2.06 (March 31, 2008)
---------------------

* Fixed problem with price chart: it didn't not show up in trading and forward testing modes
* Added another sample strategy


2.05 (March 30, 2008)
---------------------

* Charting back test period has no limitation on the period size
* Bar-based indicators are supported
* Bar-based trading, backtesting, and optimization are supported


2.04 (March 25, 2008)
---------------------

* Fixed a problem in "Brute Force" optimizer causing it to miss some parameter combinations
* Both "Brute Force" and "Divide & Conquer" optimizers have been restructured for better transparency
* Fixed email notification feature


2.03 (March 23, 2008)
---------------------

* Added a "Divide & Conquer" optimizer. This is a very fast optimizer that can handle large files and large number of parameter permutations in a reasonable amount of time: O(log2N)
* Strategy structure changed slightly in regards to how parameters are handled
* Strategy parameters must be integers
* Adjusted sample strategies


2.02 (March 18, 2008)
---------------------

* JBookTrader.properties and JBookTrader.preferences files are no longer used. Standard Java Preferences API is used instead.
* There is a new "Configure/Preferences" menu item
* Remote monitoring is now possible. JBookTrader will send notification emails when trades occur in either "Trade" or "Forward Test" modes.
* There is a new JAR distributed with the project, activation.jar. Startup script has been modified to include it in the classpath
* Fixed a bug that would show as an empty pop up error message
* Fixed a problem in optimizer: "percent completed" was not calculated correctly under certain conditions


2.01 (March 16, 2008)
---------------------

* This is a major release with significant changes
* Backtester and optimizer can handle arbitrarily large data sets
* Certain display fields moved from the main window to "Information Dialog"
* Added -XX:+UseParallelGC -XX:+AggressiveHeap? JVM options to Windows and Linux startup scripts for better performance on multi-processor machines
* Market depth is not "sampled" anymore
* Added CMEConverter class to handle CME historical market depth data
* The strategy constructor signature changed slightly to conform to the new framework
* Main window "remembers" its last size and position


1.06 (March 04, 2008)
---------------------

* Added new sample strategies with good performance
* Added new indicator
* Performance chart basic time unit is set to Second
* Commissions for stocks can have a maximum percent of trade amount
* Optimize dialog layout changed slightly


1.05 (March 02, 2008)
---------------------

* Market depth sampling rate can be configured using the marketDepth.samplingFrequency property
* Added new indicator
* Commissions are represented by a class and factory methods are provided for reuse
* Sample strategies changed slightly to reflect changes in commission representation
* Replaced "Kelly Criterion" with "True Kelly"
* Historical market depth data is no longer included with the main distribution, but can be downloaded separately


1.04 (February 28, 2008)
------------------------

* Fixed the problem which caused JBookTrader to stay in memory after exit
* Fixed number format for Forex: not enough significant digits were saved
* Loading historical data files is done in a separate thread to improve responsiveness
* Updated sample strategies
* ES data set contains 6 days of data


1.03 (February 27, 2008)
------------------------

* Instead of tracking all market depth changes, JBookTrader uses 1-second snapshots of market depth. This applies the same way to trading, backtesting, and optimizing. Historical data is also recorded in that format.
* The sample data set contains 5 days of ES 1-second market depth history
* Refactored PositionManager?. Much of the code moved to PerformanceManager?
* All types of securities are fully supported, including stocks, futures, and Forex
* Updated sample strategies


1.02 (February 25, 2008)
------------------------

* Added a more inclusive data set (3 days of ES)
* Updated user guide
* Changed MarketDepth? indicator to look smoother
* Cosmetic changes in optimizer
* Deleted Forex cash strategy: JBookTrader is not yet ready for Forex cash. However, Forex futures are fully supported, so a corresponding sample strategy was added
* Renamed and changed other sample strategies based on the changed indicator and optimization results


1.01 (February 24, 2008)
------------------------
* Initial release
