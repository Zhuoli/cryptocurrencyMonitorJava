package DataManager;

import JooqMap.tables.Sharedstockitems;
import JooqMap.tables.records.SharedstockitemsRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static JooqMap.Tables.SHAREDSTOCKITEMS;


/**
 * MYSql Connector and Executor.
 * Created by zhuolil on 8/17/16.
 */
public class DatabaseManager extends DataManager{


    private String url;
    private String userName;
    private String password;

    private DatabaseManager() {

    }

    private DatabaseManager(String url, String database, String userName, String password)
    {
        this.url = "jdbc:mysql://" + url + "/" + database;
        this.userName = userName;
        this.password = password;
    }

    public static void main(String[] args) {

    }

    /**
     * Initialize EmailMananger from XML configuration file.
     *
     * @param pathString : Configuration file path
     * @return: Emailmanager
     */
    public static DatabaseManager InitializeDatabaseManagerFromXML(String pathString) {
        Path currentPath = Paths.get("./");
        System.out.println("Currrent path: " + currentPath.toAbsolutePath());
        Path path = Paths.get("src", "resources", pathString);

        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS))
            path = Paths.get(pathString);

        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(new DataInputStream(new FileInputStream(path.toFile())));
                Element documentElement = doc.getDocumentElement();
                Element databasesNode = (Element) documentElement.getElementsByTagName("Databases").item(0);

                String url = databasesNode.getElementsByTagName("Url").item(0).getTextContent();
                String database = databasesNode.getElementsByTagName("Database").item(0).getTextContent();
                String user = databasesNode.getElementsByTagName("User").item(0).getTextContent();
                String password = databasesNode.getElementsByTagName("Password").item(0).getTextContent();

                return new DatabaseManager(url, database, user, password);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE, "Failed to read configuration file from " + pathString, e);
            }
            return new DatabaseManager();
        } else {
            return new DatabaseManager();
        }
    }

    public DatabaseManager Authenticate() throws SQLException {
        return this;
    }

    @Override
    public void WriteStockItemBackToDB(Order[] orders){
        this.getNewQueriedStockItemsFunc.get();

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "", e);
        }

        // Connection is the only JDBC resource that we need
        // PreparedStatement and ResultSet are handled by jOOQ, internally
        try (Connection conn = DriverManager.getConnection(this.url, this.userName, this.password)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
            Optional<Table<?>> table = this.GetTable(create, SHAREDSTOCKITEMS.getName());
            if (!table.isPresent()) {
                create.createTable(SHAREDSTOCKITEMS).columns(SHAREDSTOCKITEMS.fields()).execute();
            }

            // Database query result
            Result<Record> result = create.select().from(SHAREDSTOCKITEMS).fetch();

            // Map query result to StockItems
            HashMap<String, SharedstockitemsRecord> stockMap = new HashMap<>();
            result.stream().map(r -> (SharedstockitemsRecord) r).forEach(stockItem -> stockMap.put(stockItem.getSymbol(), stockItem));

            // Check each new email order
            for (Order oder : orders) {
                switch (oder.type) {
                    case BUY:
                        // If in table, update row
                        if (stockMap.containsKey(oder.Symbol)) {
                            SharedstockitemsRecord sharedStock = stockMap.get(orders);
                            int shares = sharedStock.getShares();
                            double aveCost = sharedStock.getSharedaveragecost();

                            double sum = shares * aveCost + oder.Price * oder.Shares;
                            int newShares = shares + oder.Shares;
                            double newAveCist = sum / newShares;

                            create.update(SHAREDSTOCKITEMS)
                                    .set(SHAREDSTOCKITEMS.SHAREDAVERAGECOST, newAveCist)
                                    .set(SHAREDSTOCKITEMS.SHARES, newShares)
                                    .where(SHAREDSTOCKITEMS.SYMBOL.equal(oder.Symbol))
                                    .execute();

                        } else {
                            // Else, insert this row
                            create.insertInto(SHAREDSTOCKITEMS,
                                    SHAREDSTOCKITEMS.SYMBOL, SHAREDSTOCKITEMS.SHARES, SHAREDSTOCKITEMS.SHAREDAVERAGECOST)
                                    .values(oder.Symbol, oder.Shares, oder.Price).execute();

                            System.out.println("New row inserted " + oder);
                        }
                        break;
                    case SELL:

                        // ** TO DO **
                        // Calculate the profit and notify resultPublisher
                        break;
                }
            }
        }
        // For the sake of this tutorial, let's keep exception handling simple
        catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Exception on get stock records", e);
        }
    }

    private SharedstockitemsRecord UpdateStockShares(SharedstockitemsRecord sharedStock, Order newOrder)
    {
        Assert.assertNotNull(sharedStock);
        Assert.assertNotNull(newOrder);
        Assert.assertTrue(newOrder.type == OrderType.BUY || newOrder.type == OrderType.SELL);

        if(newOrder.type == OrderType.SELL)
            Assert.assertTrue("Existing shares should not less than the selling order", sharedStock.getShares() >= newOrder.Shares);

        // Get the number of shares
        int shares = sharedStock.getShares();

        // Get average cost of each share
        double aveCost = sharedStock.getSharedaveragecost();

        // Calculate the total cost after the new order
        double sum = shares * aveCost + newOrder.Price * newOrder.Shares;

        // Update the number of shares after order
        int newShares = shares + newOrder.Shares;

        // Calculate the new average cost
        double newAveCost = sum / newShares;

        switch (newOrder.type){
            case BUY:
                break;
            case SELL:
                break;
            default:
                Assert.assertTrue("Order type not set.", false);
        }
        sharedStock.setShares(newShares);
        sharedStock.setSharedaveragecost(newAveCost);
        return sharedStock;
    }

    @Override
    public List<SharedstockitemsRecord> ReadSharedStocksFromDB() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "", e);
            return new LinkedList<>();
        }
        // Connection is the only JDBC resource that we need
        // PreparedStatement and ResultSet are handled by jOOQ, internally
        try (Connection conn = DriverManager.getConnection(this.url, this.userName, this.password)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
            Optional<Table<?>> table = this.GetTable(create, SHAREDSTOCKITEMS.getName());
            if (!table.isPresent()) {
                create.createTable(SHAREDSTOCKITEMS).columns(SHAREDSTOCKITEMS.fields()).execute();
            }
            Result<Record> result = create.select().from(SHAREDSTOCKITEMS).fetch();

            return result.stream().map(p -> (SharedstockitemsRecord) p).collect(Collectors.toList());
        }

        // For the sake of this tutorial, let's keep exception handling simple
        catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Exception on get stock records", e);
            return new LinkedList<>();
        }
    }
    public Optional<Table<?>> GetTable(DSLContext create, String tableName) {
        return create.meta().getTables().stream().filter(p -> p.getName().equals(tableName)).findFirst();
    }

}
