import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Random;

public class InventorySimulationApp {

    // --- Simulation parameters ---
    private static final double MEAN_DEMAND = 5.0;
    private static final double STD_DEMAND = 1.5;
    private static final int REORDER_POINT = 10;
    private static final int TARGET_LEVEL = 20;

    private final Random rand = new Random();
    private final DecimalFormat df2 = new DecimalFormat("#0.00");

    // --- UI components ---
    private JFrame frame;
    private JTextField daysInput;
    private DefaultTableModel tableModel;

    // Summary labels
    private JLabel sumAvgEndingInv, sumAvgLostWeek, sumAvgLostDay, sumTotalLost, sumTotalDays, sumTotalOrders;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventorySimulationApp().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Inventory Simulation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 720);
        frame.setMinimumSize(new Dimension(1000, 600));
        frame.setLayout(new BorderLayout(12, 12));
        frame.getContentPane().setBackground(new Color(245, 247, 249));

        // Top input strip
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topPanel.setBorder(new EmptyBorder(8, 12, 0, 12));
        topPanel.setBackground(new Color(245, 247, 249));
        JLabel daysLabel = new JLabel("Number of days to simulate:");
        daysLabel.setFont(daysLabel.getFont().deriveFont(Font.PLAIN, 14f));
        daysInput = new JTextField("35", 8);
        JButton runBtn = new JButton("Run Simulation");
        runBtn.setFocusPainted(false);
        runBtn.setBackground(new Color(26, 115, 232));
        runBtn.setForeground(Color.WHITE);
        runBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        runBtn.addActionListener(e -> runSimulation());

        topPanel.add(daysLabel);
        topPanel.add(daysInput);
        topPanel.add(runBtn);

        frame.add(topPanel, BorderLayout.NORTH);

        // Table columns
        String[] columns = {
                "Week", "Day", "Begin Inventory",
                "RNN for Demand", "Demand", "Ending Inventory",
                "Order Quantity", "RD for Lead time", "Lead Time",
                "Lost Sale", "Days Until order arrive"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 225, 230));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(34, 34, 34));
        table.setFont(table.getFont().deriveFont(13f));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // column widths
        int[] widths = {60, 50, 120, 120, 70, 120, 110, 120, 90, 80, 160};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[]{0,1,2,4,5,6,8,9,10}) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 216, 222)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Build bottom summary card (full-width under table)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(10, 10));
        bottomPanel.setBorder(new EmptyBorder(10, 12, 12, 12));
        bottomPanel.setBackground(new Color(245, 247, 249));

        JLabel resultTitle = new JLabel("Simulation Summary");
        resultTitle.setFont(resultTitle.getFont().deriveFont(Font.BOLD, 15f));
        resultTitle.setBorder(new EmptyBorder(6, 6, 6, 6));

        // Actual card
        JPanel card = new JPanel();
        card.setLayout(new GridBagLayout());
        card.setBackground(new Color(222, 235, 247)); // light blue
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // create labels
        sumAvgEndingInv = styledStatLabel("Average Ending Inventory: -");
        sumAvgLostWeek = styledStatLabel("Avg Lost Sales / Week: -");
        sumAvgLostDay = styledStatLabel("Avg Lost Sales / Day: -");
        sumTotalLost = styledStatLabel("Total Lost Sales: -");
        sumTotalOrders = styledStatLabel("Total Orders Placed: -");
        sumTotalDays = styledStatLabel("Total Days Simulated: -");

        // Add to card in two columns (labels on left, values on right)
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);
        leftCol.add(makeBoldLabel("Average Ending Inventory:"));
        leftCol.add(Box.createRigidArea(new Dimension(0,6)));
        leftCol.add(makeBoldLabel("Avg Lost Sales / Week:"));
        leftCol.add(Box.createRigidArea(new Dimension(0,6)));
        leftCol.add(makeBoldLabel("Avg Lost Sales / Day:"));
        leftCol.add(Box.createRigidArea(new Dimension(0,6)));
        leftCol.add(makeBoldLabel("Total Lost Sales:"));
        leftCol.add(Box.createRigidArea(new Dimension(0,6)));
        leftCol.add(makeBoldLabel("Total Orders Placed:"));
        leftCol.add(Box.createRigidArea(new Dimension(0,6)));
        leftCol.add(makeBoldLabel("Total Days Simulated:"));

        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);
        rightCol.add(sumAvgEndingInv);
        rightCol.add(Box.createRigidArea(new Dimension(0,6)));
        rightCol.add(sumAvgLostWeek);
        rightCol.add(Box.createRigidArea(new Dimension(0,6)));
        rightCol.add(sumAvgLostDay);
        rightCol.add(Box.createRigidArea(new Dimension(0,6)));
        rightCol.add(sumTotalLost);
        rightCol.add(Box.createRigidArea(new Dimension(0,6)));
        rightCol.add(sumTotalOrders);
        rightCol.add(Box.createRigidArea(new Dimension(0,6)));
        rightCol.add(sumTotalDays);

        gbc.gridx = 0;
        gbc.gridy = 0;
        card.add(leftCol, gbc);
        gbc.gridx = 1;
        card.add(rightCol, gbc);

        bottomPanel.add(resultTitle, BorderLayout.NORTH);
        bottomPanel.add(card, BorderLayout.CENTER);

        // Assemble center (table + bottom summary)
        JPanel centerPanel = new JPanel(new BorderLayout(10,10));
        centerPanel.setOpaque(false);
        centerPanel.add(scroll, BorderLayout.CENTER);
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(centerPanel, BorderLayout.CENTER);

        // footer
        JLabel footer = new JLabel("Orders placed at close of day. Lead time is integer days (0–5).");
        footer.setBorder(new EmptyBorder(6, 12, 8, 12));
        frame.add(footer, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JLabel makeBoldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setForeground(new Color(34,34,34));
        return l;
    }

    private JLabel styledStatLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 13f));
        l.setForeground(new Color(18,66,115)); 
        return l;
    }

    private void runSimulation() {
        tableModel.setRowCount(0);

        int daysToSimulate;
        try {
            daysToSimulate = Integer.parseInt(daysInput.getText().trim());
            if (daysToSimulate <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid positive integer for days.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Simulation state
        int beginInventory = 18;            // starts with 18 units
        int orderQuantity = 0;
        int leadTime = -1;                  // store the lead time value for display on the order day
        int daysUntilArrival = -1;          // -1 => no outstanding order
        boolean orderPlaced = false;

        double totalEndingInventory = 0.0;
        int totalLostSales = 0;
        int totalOrdersPlaced = 0;

        // We will print week number only once (on first day of week) and blank otherwise
        for (int day = 1; day <= daysToSimulate; day++) {
            int weekNum = (day - 1) / 5 + 1;
            String weekDisplay = (day % 5 == 1) ? String.valueOf(weekNum) : ""; // show week at first day of week (Day 1,8,...)
            // Alternative: if you want week display only once at the very first day use: (day==1)?String.valueOf(weekNum):""

            // --- Arrival at start of day ---
            if (orderPlaced && daysUntilArrival == 0) {
                // order arrives at the start of this day
                beginInventory += orderQuantity;
                orderPlaced = false;
                orderQuantity = 0;
                leadTime = -1;
                daysUntilArrival = -1;
            }

            // For display: DaysUntil show '-' if no outstanding order else the current countdown
            String daysUntilDisplay = orderPlaced ? String.valueOf(daysUntilArrival) : "-";

            // --- Generate random normal number between -2 and 1 (decimal) ---
            double rnnForDemand = -2.0 + rand.nextDouble() * 3.0; // in [-2,1)
            int demand = (int) Math.round(MEAN_DEMAND + STD_DEMAND * rnnForDemand);
            if (demand < 0) demand = 0;

            // --- Ending inventory and lost sales ---
            int lostSale = 0;
            int endingInventory;
            if (demand > beginInventory) {
                lostSale = demand - beginInventory;
                endingInventory = 0;
            } else {
                endingInventory = beginInventory - demand;
            }

           
            String rdLeadTimeStr = "";
            String orderQtyStr = "-";
            String leadTimeDisplay = "";
            if (!orderPlaced && endingInventory <= REORDER_POINT) {
                orderQuantity = TARGET_LEVEL - endingInventory;
int rdLeadTime = rand.nextInt(996) + 1;

int sampledLead;
if (rdLeadTime >= 1 && rdLeadTime <= 166)
    sampledLead = 0;
else if (rdLeadTime <= 332)
    sampledLead = 1;
else if (rdLeadTime <= 498)
    sampledLead = 2;
else if (rdLeadTime <= 664)
    sampledLead = 3;
else if (rdLeadTime <= 830)
    sampledLead = 4;
else
    sampledLead = 5;

leadTime = sampledLead;
rdLeadTimeStr = String.valueOf(rdLeadTime); // display RD number instead of random double

                // daysUntilArrival will be shown starting next day as lead+1 (so it reaches 0 then arrival next day)
                daysUntilArrival = sampledLead + 1;
                orderPlaced = true;
                orderQtyStr = String.valueOf(orderQuantity);
                leadTimeDisplay = String.valueOf(leadTime); // show lead time only for this row
                totalOrdersPlaced++;
            } else if (orderPlaced) {
                orderQtyStr = String.valueOf(orderQuantity);
                // leadTimeDisplay intentionally left blank so it does not repeat
            }

            // Add row to table
            // Week column: show only when starting the week; otherwise blank
            // Day column: show day-of-week (1..7)
            int dayOfWeek = (day - 1) % 5 + 1;
            tableModel.addRow(new Object[]{
                    weekDisplay,
                    dayOfWeek,
                    beginInventory,
                    df2.format(rnnForDemand),
                    demand,
                    endingInventory,
                    orderQtyStr,
                    rdLeadTimeStr,
                    (leadTimeDisplay.isEmpty() ? "" : leadTimeDisplay),
                    lostSale,
                    daysUntilDisplay
            });

            // accumulate stats
            totalEndingInventory += endingInventory;
            totalLostSales += lostSale;

            // --- End-of-day updates: decrement countdown so next day's start shows decreased value ---
            if (orderPlaced && daysUntilArrival > 0) {
                daysUntilArrival--;
            } else if (orderPlaced && daysUntilArrival == 0) {
                // daysUntilArrival==0 at end-of-day -> next day start will trigger arrival (handled at loop top)
                // We do not decrement here because arrival happens at next day's start when daysUntilArrival==0
            }

            // Next day's begin inventory
            beginInventory = endingInventory;
        }

        // --- Summary calculations ---
        double avgEndingInv = totalEndingInventory / daysToSimulate;
        double avgLostPerDay = (double) totalLostSales / daysToSimulate;
        double weeksSimulated = daysToSimulate / 5.0;
        double avgLostPerWeek = (weeksSimulated > 0) ? totalLostSales / weeksSimulated : 0.0;

        // update bottom summary
        sumAvgEndingInv.setText(df2.format(avgEndingInv));
        sumAvgLostWeek.setText(df2.format(avgLostPerWeek));
        sumAvgLostDay.setText(df2.format(avgLostPerDay));
        sumTotalLost.setText(String.valueOf(totalLostSales));
        sumTotalOrders.setText(String.valueOf(totalOrdersPlaced));
        sumTotalDays.setText(String.valueOf(daysToSimulate));
    }
}