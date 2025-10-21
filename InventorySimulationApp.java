// import javax.swing.*;
// import javax.swing.border.EmptyBorder;
// import javax.swing.table.DefaultTableCellRenderer;
// import javax.swing.table.DefaultTableModel;
// import java.awt.*;
// import java.text.DecimalFormat;
// import java.util.Random;

// public class InventorySimulationApp {

//     // --- Simulation parameters ---
//     private static final double MEAN_DEMAND = 5.0;
//     private static final double STD_DEMAND = 1.5;
//     private static final int REORDER_POINT = 10;
//     private static final int TARGET_LEVEL = 20;

//     private final Random rand = new Random();
//     private final DecimalFormat df2 = new DecimalFormat("#0.00");

//     // --- UI components ---
//     private JFrame frame;
//     private JTextField daysInput;
//     private DefaultTableModel tableModel;

//     // Summary labels
//     private JLabel sumAvgEndingInv, sumAvgLostWeek, sumAvgLostDay, sumTotalLost, sumTotalDays, sumTotalOrders;

//     public static void main(String[] args) {
//         SwingUtilities.invokeLater(() -> new InventorySimulationApp().createAndShowGUI());
//     }

//     private void createAndShowGUI() {
//         frame = new JFrame("Inventory Simulation System");
//         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         frame.setSize(1200, 720);
//         frame.setMinimumSize(new Dimension(1000, 600));
//         frame.setLayout(new BorderLayout(12, 12));
//         frame.getContentPane().setBackground(new Color(245, 247, 249));

//         // Top input strip
//         JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
//         topPanel.setBorder(new EmptyBorder(8, 12, 0, 12));
//         topPanel.setBackground(new Color(245, 247, 249));
//         JLabel daysLabel = new JLabel("Number of days to simulate:");
//         daysLabel.setFont(daysLabel.getFont().deriveFont(Font.PLAIN, 14f));
//         daysInput = new JTextField("35", 8);
//         JButton runBtn = new JButton("Run Simulation");
//         runBtn.setFocusPainted(false);
//         runBtn.setBackground(new Color(26, 115, 232));
//         runBtn.setForeground(Color.WHITE);
//         runBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
//         runBtn.addActionListener(e -> runSimulation());

//         topPanel.add(daysLabel);
//         topPanel.add(daysInput);
//         topPanel.add(runBtn);

//         frame.add(topPanel, BorderLayout.NORTH);

//         // Table columns
//         String[] columns = {
//                 "Week", "Day", "Begin Inventory",
//                 "RNN for Demand", "Demand", "Ending Inventory",
//                 "Order Quantity", "RD for Lead time", "Lead Time",
//                 "Lost Sale", "Days Until order arrive"
//         };
//         tableModel = new DefaultTableModel(columns, 0) {
//             @Override
//             public boolean isCellEditable(int r, int c) { return false; }
//         };

//         JTable table = new JTable(tableModel);
//         table.setRowHeight(26);
//         table.setShowGrid(true);
//         table.setGridColor(new Color(220, 225, 230));
//         table.setFillsViewportHeight(true);
//         table.setBackground(Color.WHITE);
//         table.setForeground(new Color(34, 34, 34));
//         table.setFont(table.getFont().deriveFont(13f));
//         table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

//         // column widths
//         int[] widths = {60, 50, 120, 120, 70, 120, 110, 120, 90, 80, 160};
//         for (int i = 0; i < widths.length; i++) {
//             table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
//         }

//         // alignment
//         DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
//         centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
//         for (int i : new int[]{0,1,2,4,5,6,8,9,10}) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

//         DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
//         rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
//         table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
//         table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

//         JScrollPane scroll = new JScrollPane(table);
//         scroll.setBorder(BorderFactory.createCompoundBorder(
//                 BorderFactory.createLineBorder(new Color(210, 216, 222)),
//                 BorderFactory.createEmptyBorder(8, 8, 8, 8)
//         ));
//         scroll.getVerticalScrollBar().setUnitIncrement(16);

//         // Build bottom summary card (full-width under table)
//         JPanel bottomPanel = new JPanel();
//         bottomPanel.setLayout(new BorderLayout(10, 10));
//         bottomPanel.setBorder(new EmptyBorder(10, 12, 12, 12));
//         bottomPanel.setBackground(new Color(245, 247, 249));

//         JLabel resultTitle = new JLabel("Simulation Summary");
//         resultTitle.setFont(resultTitle.getFont().deriveFont(Font.BOLD, 15f));
//         resultTitle.setBorder(new EmptyBorder(6, 6, 6, 6));

//         // Actual card
//         JPanel card = new JPanel();
//         card.setLayout(new GridBagLayout());
//         card.setBackground(new Color(222, 235, 247)); // light blue
//         card.setBorder(BorderFactory.createCompoundBorder(
//                 BorderFactory.createLineBorder(new Color(180, 200, 220)),
//                 BorderFactory.createEmptyBorder(12, 12, 12, 12)
//         ));

//         GridBagConstraints gbc = new GridBagConstraints();
//         gbc.insets = new Insets(8, 12, 8, 12);
//         gbc.anchor = GridBagConstraints.WEST;
//         gbc.fill = GridBagConstraints.HORIZONTAL;
//         gbc.gridx = 0;
//         gbc.gridy = 0;

//         // create labels
//         sumAvgEndingInv = styledStatLabel("Average Ending Inventory: -");
//         sumAvgLostWeek = styledStatLabel("Avg Lost Sales / Week: -");
//         sumAvgLostDay = styledStatLabel("Avg Lost Sales / Day: -");
//         sumTotalLost = styledStatLabel("Total Lost Sales: -");
//         sumTotalOrders = styledStatLabel("Total Orders Placed: -");
//         sumTotalDays = styledStatLabel("Total Days Simulated: -");

//         // Add to card in two columns (labels on left, values on right)
//         JPanel leftCol = new JPanel();
//         leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
//         leftCol.setOpaque(false);
//         leftCol.add(makeBoldLabel("Average Ending Inventory:"));
//         leftCol.add(Box.createRigidArea(new Dimension(0,6)));
//         leftCol.add(makeBoldLabel("Avg Lost Sales / Week:"));
//         leftCol.add(Box.createRigidArea(new Dimension(0,6)));
//         leftCol.add(makeBoldLabel("Avg Lost Sales / Day:"));
//         leftCol.add(Box.createRigidArea(new Dimension(0,6)));
//         leftCol.add(makeBoldLabel("Total Lost Sales:"));
//         leftCol.add(Box.createRigidArea(new Dimension(0,6)));
//         leftCol.add(makeBoldLabel("Total Orders Placed:"));
//         leftCol.add(Box.createRigidArea(new Dimension(0,6)));
//         leftCol.add(makeBoldLabel("Total Days Simulated:"));

//         JPanel rightCol = new JPanel();
//         rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
//         rightCol.setOpaque(false);
//         rightCol.add(sumAvgEndingInv);
//         rightCol.add(Box.createRigidArea(new Dimension(0,6)));
//         rightCol.add(sumAvgLostWeek);
//         rightCol.add(Box.createRigidArea(new Dimension(0,6)));
//         rightCol.add(sumAvgLostDay);
//         rightCol.add(Box.createRigidArea(new Dimension(0,6)));
//         rightCol.add(sumTotalLost);
//         rightCol.add(Box.createRigidArea(new Dimension(0,6)));
//         rightCol.add(sumTotalOrders);
//         rightCol.add(Box.createRigidArea(new Dimension(0,6)));
//         rightCol.add(sumTotalDays);

//         gbc.gridx = 0;
//         gbc.gridy = 0;
//         card.add(leftCol, gbc);
//         gbc.gridx = 1;
//         card.add(rightCol, gbc);

//         bottomPanel.add(resultTitle, BorderLayout.NORTH);
//         bottomPanel.add(card, BorderLayout.CENTER);

//         // Assemble center (table + bottom summary)
//         JPanel centerPanel = new JPanel(new BorderLayout(10,10));
//         centerPanel.setOpaque(false);
//         centerPanel.add(scroll, BorderLayout.CENTER);
//         centerPanel.add(bottomPanel, BorderLayout.SOUTH);

//         frame.add(centerPanel, BorderLayout.CENTER);

//         // footer
//         JLabel footer = new JLabel("Orders placed at close of day. Lead time is integer days (0–5).");
//         footer.setBorder(new EmptyBorder(6, 12, 8, 12));
//         frame.add(footer, BorderLayout.SOUTH);

//         frame.setLocationRelativeTo(null);
//         frame.setVisible(true);
//     }

//     private JLabel makeBoldLabel(String text) {
//         JLabel l = new JLabel(text);
//         l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
//         l.setForeground(new Color(34,34,34));
//         return l;
//     }

//     private JLabel styledStatLabel(String text) {
//         JLabel l = new JLabel(text);
//         l.setFont(l.getFont().deriveFont(Font.PLAIN, 13f));
//         l.setForeground(new Color(18,66,115)); 
//         return l;
//     }

//     private void runSimulation() {
//         tableModel.setRowCount(0);

//         int daysToSimulate;
//         try {
//             daysToSimulate = Integer.parseInt(daysInput.getText().trim());
//             if (daysToSimulate <= 0) throw new NumberFormatException();
//         } catch (NumberFormatException ex) {
//             JOptionPane.showMessageDialog(frame, "Please enter a valid positive integer for days.",
//                     "Invalid Input", JOptionPane.ERROR_MESSAGE);
//             return;
//         }

//         // Simulation state
//         int beginInventory = 18;            // starts with 18 units
//         int orderQuantity = 0;
//         int leadTime = -1;                  // store the lead time value for display on the order day
//         int daysUntilArrival = -1;          // -1 => no outstanding order
//         boolean orderPlaced = false;

//         double totalEndingInventory = 0.0;
//         int totalLostSales = 0;
//         int totalOrdersPlaced = 0;

//         // We will print week number only once (on first day of week) and blank otherwise
//         for (int day = 1; day <= daysToSimulate; day++) {
//             int weekNum = (day - 1) / 5 + 1;
//             String weekDisplay = (day % 5 == 1) ? String.valueOf(weekNum) : ""; // show week at first day of week (Day 1,8,...)
//             // Alternative: if you want week display only once at the very first day use: (day==1)?String.valueOf(weekNum):""

//             // --- Arrival at start of day ---
//             if (orderPlaced && daysUntilArrival == 0) {
//                 // order arrives at the start of this day
//                 beginInventory += orderQuantity;
//                 orderPlaced = false;
//                 orderQuantity = 0;
//                 leadTime = -1;
//                 daysUntilArrival = -1;
//             }

//             // For display: DaysUntil show '-' if no outstanding order else the current countdown
//             String daysUntilDisplay = orderPlaced ? String.valueOf(daysUntilArrival) : "-";

//             // --- Generate random normal number between -2 and 1 (decimal) ---
//             double rnnForDemand = -2.0 + rand.nextDouble() * 3.0; // in [-2,1)
//             int demand = (int) Math.round(MEAN_DEMAND + STD_DEMAND * rnnForDemand);
//             if (demand < 0) demand = 0;

//             // --- Ending inventory and lost sales ---
//             int lostSale = 0;
//             int endingInventory;
//             if (demand > beginInventory) {
//                 lostSale = demand - beginInventory;
//                 endingInventory = 0;
//             } else {
//                 endingInventory = beginInventory - demand;
//             }

           
//             String rdLeadTimeStr = "";
//             String orderQtyStr = "-";
//             String leadTimeDisplay = "";
//             if (!orderPlaced && endingInventory <= REORDER_POINT) {
//                 orderQuantity = TARGET_LEVEL - endingInventory;
// int rdLeadTime = rand.nextInt(996) + 1;

// int sampledLead;
// if (rdLeadTime >= 1 && rdLeadTime <= 166)
//     sampledLead = 0;
// else if (rdLeadTime <= 332)
//     sampledLead = 1;
// else if (rdLeadTime <= 498)
//     sampledLead = 2;
// else if (rdLeadTime <= 664)
//     sampledLead = 3;
// else if (rdLeadTime <= 830)
//     sampledLead = 4;
// else
//     sampledLead = 5;

// leadTime = sampledLead;
// rdLeadTimeStr = String.valueOf(rdLeadTime); // display RD number instead of random double

//                 // daysUntilArrival will be shown starting next day as lead+1 (so it reaches 0 then arrival next day)
//                 daysUntilArrival = sampledLead + 1;
//                 orderPlaced = true;
//                 orderQtyStr = String.valueOf(orderQuantity);
//                 leadTimeDisplay = String.valueOf(leadTime); // show lead time only for this row
//                 totalOrdersPlaced++;
//             } else if (orderPlaced) {
//                 orderQtyStr = String.valueOf(orderQuantity);
//                 // leadTimeDisplay intentionally left blank so it does not repeat
//             }

//             // Add row to table
//             // Week column: show only when starting the week; otherwise blank
//             // Day column: show day-of-week (1..7)
//             int dayOfWeek = (day - 1) % 5 + 1;
//             tableModel.addRow(new Object[]{
//                     weekDisplay,
//                     dayOfWeek,
//                     beginInventory,
//                     df2.format(rnnForDemand),
//                     demand,
//                     endingInventory,
//                     orderQtyStr,
//                     rdLeadTimeStr,
//                     (leadTimeDisplay.isEmpty() ? "" : leadTimeDisplay),
//                     lostSale,
//                     daysUntilDisplay
//             });

//             // accumulate stats
//             totalEndingInventory += endingInventory;
//             totalLostSales += lostSale;

//             // --- End-of-day updates: decrement countdown so next day's start shows decreased value ---
//             if (orderPlaced && daysUntilArrival > 0) {
//                 daysUntilArrival--;
//             } else if (orderPlaced && daysUntilArrival == 0) {
//                 // daysUntilArrival==0 at end-of-day -> next day start will trigger arrival (handled at loop top)
//                 // We do not decrement here because arrival happens at next day's start when daysUntilArrival==0
//             }

//             // Next day's begin inventory
//             beginInventory = endingInventory;
//         }

//         // --- Summary calculations ---
//         double avgEndingInv = totalEndingInventory / daysToSimulate;
//         double avgLostPerDay = (double) totalLostSales / daysToSimulate;
//         double weeksSimulated = daysToSimulate / 5.0;
//         double avgLostPerWeek = (weeksSimulated > 0) ? totalLostSales / weeksSimulated : 0.0;

//         // update bottom summary
//         sumAvgEndingInv.setText(df2.format(avgEndingInv));
//         sumAvgLostWeek.setText(df2.format(avgLostPerWeek));
//         sumAvgLostDay.setText(df2.format(avgLostPerDay));
//         sumTotalLost.setText(String.valueOf(totalLostSales));
//         sumTotalOrders.setText(String.valueOf(totalOrdersPlaced));
//         sumTotalDays.setText(String.valueOf(daysToSimulate));
//     }
// }



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
    private static final int TARGET_LEVEL = 20;

    private final Random rand = new Random();
    private final DecimalFormat df2 = new DecimalFormat("#0.00");

    // --- UI components ---
    private JFrame frame;
    private JTextField daysInput;
    private JTextField initialInventoryInput;
    private JTextField reorderPointInput;
    private DefaultTableModel tableModel;
    private JLabel sumAvgEndingInv, sumAvgLostWeek, sumAvgLostDay, sumTotalLost, sumTotalDays, sumTotalOrders;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventorySimulationApp().createAndShowGUI());
    }

    private void createAndShowGUI() {
        // Set look and feel for better cross-platform consistency
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("Inventory Simulation System") {
            @Override
            public void paintComponents(Graphics g) {
                super.paintComponents(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(200, 220, 255), 0, getHeight(), new Color(230, 240, 255));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 800);
        frame.setMinimumSize(new Dimension(1000, 650));
        frame.setLayout(new BorderLayout(16, 16));

        // Top input panel (Card-style)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 12));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 180, 220), 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));
        topPanel.setBackground(new Color(245, 250, 255));

        // Days input
        JLabel daysLabel = new JLabel("Simulation Days:");
        daysLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        daysLabel.setForeground(new Color(33, 37, 41));

        daysInput = new JTextField("35", 8);
        daysInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        daysInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 180, 220), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        daysInput.setToolTipText("Enter number of days to simulate (positive integer)");
        daysInput.setBackground(new Color(255, 255, 255));

        // Initial inventory input
        JLabel initialInventoryLabel = new JLabel("Initial Inventory:");
        initialInventoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        initialInventoryLabel.setForeground(new Color(33, 37, 41));

        initialInventoryInput = new JTextField("18", 8);
        initialInventoryInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        initialInventoryInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 180, 220), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        initialInventoryInput.setToolTipText("Enter initial inventory (non-negative integer)");
        initialInventoryInput.setBackground(new Color(255, 255, 255));

        // Reorder point input
        JLabel reorderPointLabel = new JLabel("Reorder Point:");
        reorderPointLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        reorderPointLabel.setForeground(new Color(33, 37, 41));

        reorderPointInput = new JTextField("10", 8);
        reorderPointInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reorderPointInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 180, 220), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        reorderPointInput.setToolTipText("Enter reorder point (non-negative integer)");
        reorderPointInput.setBackground(new Color(255, 255, 255));

        // Run button with gradient and shadow
        JButton runBtn = new JButton("Run Simulation") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(52, 152, 219), 0, getHeight(), new Color(41, 128, 185));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        runBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        runBtn.setForeground(Color.WHITE);
        runBtn.setFocusPainted(false);
        runBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        runBtn.setContentAreaFilled(false);
        runBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        runBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                runBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(41, 128, 185), 1, true),
                    new EmptyBorder(8, 16, 8, 16)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                runBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                runBtn.setBackground(new Color(41, 128, 185));
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                runBtn.setBackground(new Color(52, 152, 219));
            }
        });
        runBtn.addActionListener(e -> runSimulation());

        topPanel.add(daysLabel);
        topPanel.add(daysInput);
        topPanel.add(initialInventoryLabel);
        topPanel.add(initialInventoryInput);
        topPanel.add(reorderPointLabel);
        topPanel.add(reorderPointInput);
        topPanel.add(runBtn);
        frame.add(topPanel, BorderLayout.NORTH);

        // Center panel with table title and table
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(240, 242, 245));
        centerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

        // Table title
        JLabel tableTitle = new JLabel("Simulation Data");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(new Color(33, 37, 41));
        tableTitle.setBorder(new EmptyBorder(8, 8, 8, 8));
        centerPanel.add(tableTitle, BorderLayout.NORTH);

        // Table setup
        String[] columns = {
            "Week", "Day", "Begin Inventory", "RNN for Demand", "Demand",
            "Ending Inventory", "Order Quantity", "RD for Lead time",
            "Lead Time", "Lost Sale", "Days Until Order Arrives"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 210, 220));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(33, 37, 41));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Custom renderer for alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 240, 255));
                return c;
            }
        });

        // Enhanced table header styling with tooltips
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(52, 152, 219));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(150, 180, 220)));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getWidth(), 36));

        // Add tooltips to column headers
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    label.setBackground(new Color(52, 152, 219));
                    label.setForeground(Color.WHITE);
                    label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(150, 180, 220)),
                        new EmptyBorder(8, 8, 8, 8)
                    ));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    // Set tooltips for each column
                    switch (column) {
                        case 0: label.setToolTipText("Week number of the simulation"); break;
                        case 1: label.setToolTipText("Day within the week (1-5)"); break;
                        case 2: label.setToolTipText("Inventory at start of day"); break;
                        case 3: label.setToolTipText("Random normal number for demand calculation"); break;
                        case 4: label.setToolTipText("Demand for the day"); break;
                        case 5: label.setToolTipText("Inventory at end of day"); break;
                        case 6: label.setToolTipText("Quantity ordered, if any"); break;
                        case 7: label.setToolTipText("Random number for lead time calculation"); break;
                        case 8: label.setToolTipText("Lead time for the order"); break;
                        case 9: label.setToolTipText("Lost sales due to insufficient inventory"); break;
                        case 10: label.setToolTipText("Days until the ordered inventory arrives"); break;
                    }
                    return label;
                }
            });
        }

        // Column widths
        int[] widths = {80, 60, 120, 120, 80, 120, 120, 120, 100, 100, 160};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Column alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[]{0, 1, 2, 4, 5, 6, 8, 9, 10}) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        centerPanel.add(scroll, BorderLayout.CENTER);
        frame.add(centerPanel, BorderLayout.CENTER);

        // Summary panel (Card-style)
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        bottomPanel.setBackground(new Color(240, 242, 245));

        JLabel resultTitle = new JLabel("Simulation Results");
        resultTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultTitle.setForeground(new Color(33, 37, 41));
        resultTitle.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel card = new JPanel(new GridLayout(2, 3, 20, 12));
        card.setBackground(new Color(245, 250, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 180, 220), 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        // Summary labels with tooltips
        sumAvgEndingInv = styledStatLabel("0.00", "Average inventory at end of day");
        sumAvgLostWeek = styledStatLabel("0.00", "Average lost sales per week");
        sumAvgLostDay = styledStatLabel("0.00", "Average lost sales per day");
        sumTotalLost = styledStatLabel("0", "Total lost sales during simulation");
        sumTotalOrders = styledStatLabel("0", "Total number of orders placed");
        sumTotalDays = styledStatLabel("0", "Total days simulated");

        // Create stat panels
        card.add(createStatPanel("Avg. Ending Inventory", sumAvgEndingInv));
        card.add(createStatPanel("Avg. Lost Sales/Week", sumAvgLostWeek));
        card.add(createStatPanel("Avg. Lost Sales/Day", sumAvgLostDay));
        card.add(createStatPanel("Total Lost Sales", sumTotalLost));
        card.add(createStatPanel("Total Orders Placed", sumTotalOrders));
        card.add(createStatPanel("Total Days Simulated", sumTotalDays));

        bottomPanel.add(resultTitle, BorderLayout.NORTH);
        bottomPanel.add(card, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createStatPanel(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(8, 4));
        panel.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(33, 37, 41));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private JLabel styledStatLabel(String text, String tooltip) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(41, 128, 185));
        label.setToolTipText(tooltip);
        return label;
    }

    private void runSimulation() {
        tableModel.setRowCount(0);

        int daysToSimulate;
        int beginInventory;
        int reorderPoint;

        // Validate days input
        try {
            daysToSimulate = Integer.parseInt(daysInput.getText().trim());
            if (daysToSimulate <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            daysInput.setBorder(BorderFactory.createLineBorder(new Color(231, 76, 60), 2));
            JOptionPane.showMessageDialog(frame,
                "Please enter a valid positive integer for days.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        daysInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 180, 220), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));

        // Validate initial inventory input
        try {
            beginInventory = Integer.parseInt(initialInventoryInput.getText().trim());
            if (beginInventory < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            initialInventoryInput.setBorder(BorderFactory.createLineBorder(new Color(231, 76, 60), 2));
            JOptionPane.showMessageDialog(frame,
                "Please enter a valid non-negative integer for initial inventory.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        initialInventoryInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 180, 220), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));

        // Validate reorder point input
        try {
            reorderPoint = Integer.parseInt(reorderPointInput.getText().trim());
            if (reorderPoint < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            reorderPointInput.setBorder(BorderFactory.createLineBorder(new Color(231, 76, 60), 2));
            JOptionPane.showMessageDialog(frame,
                "Please enter a valid non-negative integer for reorder point.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        reorderPointInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 180, 220), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));

        // Simulation state
        int orderQuantity = 0;
        int leadTime = -1;
        int daysUntilArrival = -1;
        boolean orderPlaced = false;

        double totalEndingInventory = 0.0;
        int totalLostSales = 0;
        int totalOrdersPlaced = 0;

        for (int day = 1; day <= daysToSimulate; day++) {
            int weekNum = (day - 1) / 5 + 1;
            String weekDisplay = (day % 5 == 1 || day == 1) ? String.valueOf(weekNum) : "";
            if (orderPlaced && daysUntilArrival == 0) {
                beginInventory += orderQuantity;
                orderPlaced = false;
                orderQuantity = 0;
                leadTime = -1;
                daysUntilArrival = -1;
            }

            String daysUntilDisplay = orderPlaced ? String.valueOf(daysUntilArrival) : "-";
            double rnnForDemand = -2.0 + rand.nextDouble() * 3.0;
            int demand = (int) Math.round(MEAN_DEMAND + STD_DEMAND * rnnForDemand);
            if (demand < 0) demand = 0;

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
            if (!orderPlaced && endingInventory <= reorderPoint) {
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
                rdLeadTimeStr = String.valueOf(rdLeadTime);
                daysUntilArrival = sampledLead + 1;
                orderPlaced = true;
                orderQtyStr = String.valueOf(orderQuantity);
                leadTimeDisplay = String.valueOf(leadTime);
                totalOrdersPlaced++;
            } else if (orderPlaced) {
                orderQtyStr = String.valueOf(orderQuantity);
            }

            // Correct day-of-week calculation (1 to 5 per week)
            int dayOfWeek = (day - 1) % 5 + 1;
            tableModel.addRow(new Object[]{
                weekDisplay, dayOfWeek, beginInventory, df2.format(rnnForDemand),
                demand, endingInventory, orderQtyStr, rdLeadTimeStr,
                leadTimeDisplay, lostSale, daysUntilDisplay
            });

            totalEndingInventory += endingInventory;
            totalLostSales += lostSale;

            if (orderPlaced && daysUntilArrival > 0) {
                daysUntilArrival--;
            }
            beginInventory = endingInventory;
        }

        // Summary calculations
        double avgEndingInv = totalEndingInventory / daysToSimulate;
        double avgLostPerDay = (double) totalLostSales / daysToSimulate;
        double weeksSimulated = daysToSimulate / 5.0;
        double avgLostPerWeek = (weeksSimulated > 0) ? totalLostSales / weeksSimulated : 0.0;

        sumAvgEndingInv.setText(df2.format(avgEndingInv));
        sumAvgLostWeek.setText(df2.format(avgLostPerWeek));
        sumAvgLostDay.setText(df2.format(avgLostPerDay));
        sumTotalLost.setText(String.valueOf(totalLostSales));
        sumTotalOrders.setText(String.valueOf(totalOrdersPlaced));
        sumTotalDays.setText(String.valueOf(daysToSimulate));
    }
}