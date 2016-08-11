import DataManager.DataManager;
import PriceMonitor.PriceMonitor;
import ResultPublisher.ResultPublisher;
import com.joanzapata.utils.Strings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * Created by zhuoli on 6/23/16.
 */
public class StockMaster {

    public static void main(String[] args) {
        StockMaster.SetUp();
        try {
            new StockMaster().start(args);
            return;
        } catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "Unexpected exception", exc);
            exc.printStackTrace();
        }
        System.exit(1);
    }

    /**
     * Sets up the environment.
     */
    public static void SetUp() {
        try {
            String filePath = Strings.format("./{classname}.log").with("classname", StockMaster.class.getName()).build();
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            Handler fileHandler = new FileHandler(filePath);

            // Set file log format to plain text
            fileHandler.setFormatter(simpleFormatter);
            Logger.getGlobal().addHandler(fileHandler);
            fileHandler.setLevel(Level.ALL);
            System.out.println("Log setup succeed, log file stored at " + filePath);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Entry of the StockMaster.
     *
     * @param args
     * @throws Exception
     */
    public void start(String[] args) throws Exception {

        // Log start time
        Logger.getGlobal().info("CurrencyProphet been launched. all rights reserved");

        // Task executor
        ExecutorService taskExecutor = Executors.newFixedThreadPool(3);

        // Initialize result publisher and authenticate user information
        ResultPublisher publisher = ResultPublisher.GetInstance(PriceMonitor::GetStocks).CollectInformationAndAuthenticate();

        System.out.println("Email authenticate succeed");
        System.out.println("Monitor started...");

        // Initialize price monitor
        PriceMonitor priceMonitor = new PriceMonitor();

        // Initialize data manager and StockRegister method
        DataManager dataManager = new DataManager(PriceMonitor::RegisterStockSymboles);

        // Submit result publisher thread
        taskExecutor.submit(() -> {
            publisher.Start();
        });

        // Submit data manager thread
        taskExecutor.submit(() -> {
            dataManager.Start();
        });

        // Submit Price monitor thread
        taskExecutor.submit(() -> {
            try {
                priceMonitor.Start();
            } catch (InterruptedException e) {
                Logger.getGlobal().log(Level.SEVERE, "PriceMonitor thread has crashed for the reason" + e.getMessage(), e);
            }
        });

        // Wait for all threads done
        taskExecutor.shutdown();

        // Log for Abnormal state
        Logger.getGlobal().severe("System shutdown");
    }
}
