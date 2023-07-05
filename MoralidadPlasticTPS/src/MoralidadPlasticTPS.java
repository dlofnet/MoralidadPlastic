import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EtchedBorder;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MoralidadPlasticTPS {

	private JFrame frmMoralidadPlasticProducts;
	private JTextField textField;
	private JTable tableInventory;
	private JTextField textQuantity;
	private JTable tableTransaction;
	private JTextField textField_2;
	private DefaultTableModel model_inventory, model_transaction;
	private String inventoryInfo = "inventoryInfo.txt", transactionInfo = "transactionInfo.txt";
	private String item, unit, quantity, unitPrice, grandTotal, newLine, transactionItem, modified, originalLine, entry;
	private BufferedReader br;
	private LocalDateTime now;
	private DateTimeFormatter formatter;
	private String formattedDateTime;
	private JButton btnInAdd, btnEdit, btnInDelete, btnSearch, btnAdd, btnNew, btnDelete, btnClear, btnSave, btnCancel;
	private File inventoryFile, transactionFile;
	private int transactionQuantity, newQuantity, totalPrice;

	/**
	 * Launch the application.
	 * -----------------------------------------------------------------------------------------------------------------------------------------
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MoralidadPlasticTPS window = new MoralidadPlasticTPS();
					window.frmMoralidadPlasticProducts.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * -----------------------------------------------------------------------------------------------------------------------------------------
	 */
	public MoralidadPlasticTPS() {
		initialize();
	}

	/**
	 * Delete and Edit inventory content.
	 * -----------------------------------------------------------------------------------------------------------------------------------------
	 */
	private static List<String> readTextFile(String filePath) {
		List<String> lines = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return lines;
	}

	private static void writeTextFile(String filePath, List<String> lines) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			for (String line : lines) {
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void removeLineContaining(List<String> lines, String text) {
		lines.removeIf(line -> line.contains(text));
	}

	private static void updateLine(List<String> lines, String originalName, String newName) {
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).equals(originalName)) {
				lines.set(i, newName);
				break;
			}
		}
	}

	private static String findLine(String filePath, int lineNumber) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line;
			int currentLine = 1;

			while ((line = reader.readLine()) != null) {
				if (currentLine == lineNumber) {
					return line;
				}

				currentLine++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null; // Line not found
	}

	private static String findLine(String filePath, String keyword) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.contains(keyword)) {
					return line;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null; // Line not found
	}

	public void returnToInventory(String keyword, int quantity) {
		String line = findLine(inventoryInfo, keyword);

		if (line != null) {
			String[] returnItem = line.split("/");
			returnItem[2] = "" + (quantity + Integer.parseInt(returnItem[2]));
			String combinedString = String.join("/", returnItem);
			edit(inventoryInfo, line, combinedString);
			updateInventory();
		} else {
			System.out.println("Line not found for the search keyword: " + keyword);
		}
	}

	public String modifyLine() {
		int row = tableInventory.getSelectedRow();
		item = model_inventory.getValueAt(row, 0).toString();
		unit = model_inventory.getValueAt(row, 1).toString();
		unitPrice = model_inventory.getValueAt(row, 3).toString();
		quantity = model_inventory.getValueAt(row, 2).toString();
		return newLine = item + "/" + unit + "/" + quantity + "/" + unitPrice;
	}

	public String modifyLine(int transactionQuantity) {
		int row = tableInventory.getSelectedRow();
		item = model_inventory.getValueAt(row, 0).toString();
		unit = model_inventory.getValueAt(row, 1).toString();
		unitPrice = model_inventory.getValueAt(row, 3).toString();
		quantity = model_inventory.getValueAt(row, 2).toString();
		if (Integer.parseInt(quantity) - transactionQuantity > 0) {
			return newLine = item + "/" + quantity + "/" + (Integer.parseInt(quantity) - transactionQuantity) + "/"
					+ unitPrice;
		} else {
			return "error";
		}
	}

	public void edit(String filePath, String originalLine, String newLine) {
		List<String> lines = readTextFile(filePath);
		updateLine(lines, originalLine, newLine);
		writeTextFile(filePath, lines);
	}

	public void delete(String filePath, String lineToRemove) {
		List<String> lines = readTextFile(filePath);
		removeLineContaining(lines, lineToRemove);
		writeTextFile(filePath, lines);
	}

	public void clearItem(int row) {
		
		String item = model_transaction.getValueAt(row, 0).toString();
		String quantity = model_transaction.getValueAt(row, 1).toString();
		String unitPrice = model_transaction.getValueAt(row, 2).toString();
		String totalPrice = model_transaction.getValueAt(row, 3).toString();
		String transactionItem = item + "/" + quantity + "/" + unitPrice + "/" + totalPrice;
		delete(transactionInfo, transactionItem);

		returnToInventory(item, Integer.parseInt(quantity));

		model_transaction.removeRow(row);

	}

	/**
	 * Update content.
	 * -----------------------------------------------------------------------------------------------------------------------------------------
	 */
	public void updateInventory() {

		inventoryFile = new File(inventoryInfo);
		model_inventory.setRowCount(0);

		try {
			br = new BufferedReader(new FileReader(inventoryFile));
			Object[] tableLines = br.lines().toArray();

			for (int i = 0; i < tableLines.length; i++) {
				String line = tableLines[i].toString().trim();
				String[] dataRow = line.split("/");
				model_inventory.addRow(dataRow);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	public void updateTransaction() {

		model_transaction.setRowCount(0);

		try {
			br = new BufferedReader(new FileReader(transactionInfo));
			Object[] tableLines = br.lines().toArray();

			for (int i = 0; i < tableLines.length; i++) {
				String line = tableLines[i].toString().trim();
				String[] dataRow = line.split("/");
				model_transaction.addRow(dataRow);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public int total() {
		int totalSum = 0;

		for (int row = 0; row < tableTransaction.getRowCount(); row++) {
			String value = tableTransaction.getValueAt(row, 3).toString();
			int price = Integer.parseInt(value);
			totalSum += price;
		}

		return totalSum;
	}

	/**
	 * Initialize the contents of the frame.
	 * -----------------------------------------------------------------------------------------------------------------------------------------
	 */
	private void initialize() {

		/**
		 * Components of the frame.
		 * -------------------------------------------------------------------------------------------------------
		 */

		frmMoralidadPlasticProducts = new JFrame();
		frmMoralidadPlasticProducts.setResizable(false);
		frmMoralidadPlasticProducts.setTitle("Moralidad Plastic Products");
		frmMoralidadPlasticProducts.setBounds(100, 50, 1280, 720);
		frmMoralidadPlasticProducts.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmMoralidadPlasticProducts.getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel("MORALIDAD PLASTIC PRODUCTS");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 30));
		lblNewLabel.setBounds(46, 25, 667, 42);
		frmMoralidadPlasticProducts.getContentPane().add(lblNewLabel);

		/**
		 * panelInventory
		 * -------------------------------------------------------------------------------------------------------
		 */

		JPanel panelInventory = new JPanel();
		panelInventory.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Inventory",
				TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelInventory.setBounds(46, 89, 560, 554);
		frmMoralidadPlasticProducts.getContentPane().add(panelInventory);
		panelInventory.setLayout(null);

		/**
		 * panelTransaction
		 * -------------------------------------------------------------------------------------------------------
		 */

		JPanel panelTransaction = new JPanel();
		panelTransaction.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Transaction", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelTransaction.setBounds(713, 89, 524, 554);
		frmMoralidadPlasticProducts.getContentPane().add(panelTransaction);
		panelTransaction.setLayout(null);

		JLabel lblTotal = new JLabel("Total:");
		lblTotal.setBounds(20, 459, 109, 13);
		panelTransaction.add(lblTotal);

		JLabel lblNewLabel_1_1 = new JLabel("Amount received:");
		lblNewLabel_1_1.setBounds(20, 482, 109, 13);
		panelTransaction.add(lblNewLabel_1_1);

		JLabel lblNewLabel_1_1_1 = new JLabel("Change:");
		lblNewLabel_1_1_1.setBounds(20, 505, 109, 13);
		panelTransaction.add(lblNewLabel_1_1_1);

		textField = new JTextField();
		textField.setBounds(20, 28, 416, 19);
		panelInventory.add(textField);
		textField.setColumns(10);

		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setBounds(139, 479, 87, 19);
		panelTransaction.add(textField_2);

		textQuantity = new JTextField();
		textQuantity.setText("Quantity");
		textQuantity.setBounds(616, 329, 87, 19);
		frmMoralidadPlasticProducts.getContentPane().add(textQuantity);
		textQuantity.setColumns(10);

		/**
		 * Buttons for operations.
		 */
		btnAdd = new JButton(">>>");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tableInventory.getSelectedRowCount() == 0 || tableInventory.getSelectedRowCount() > 1) {
					JOptionPane.showMessageDialog(null, "Select one item.");
				} else {

					entry = textQuantity.getText();
					if (!entry.matches("-?\\d+") || Integer.parseInt(entry) <= 0) {
						JOptionPane.showMessageDialog(null, "Enter a valid number in the quantity field.");
					} else {
						int row = tableInventory.getSelectedRow();

						item = model_inventory.getValueAt(row, 0).toString();
						unit = model_inventory.getValueAt(row, 1).toString();
						quantity = model_inventory.getValueAt(row, 2).toString();
						unitPrice = (String) model_inventory.getValueAt(row, 3);
						transactionQuantity = Integer.parseInt(entry);
						newQuantity = Integer.parseInt(quantity) - transactionQuantity;
						totalPrice = Integer.parseInt(unitPrice) * transactionQuantity;

						originalLine = findLine(inventoryInfo, tableInventory.getSelectedRow() + 1);
						String newLine = item + "/" + unit + "/" + newQuantity + "/" + unitPrice;
						modified = modifyLine(transactionQuantity);

						if (!modified.equals("error")) {
							edit(inventoryInfo, originalLine, newLine);
							updateInventory();

							transactionItem = item + "/" + transactionQuantity + "/" + unitPrice + "/" + totalPrice;

							try {
								FileWriter fw = new FileWriter(transactionInfo, true);
								BufferedWriter bw = new BufferedWriter(fw);

								bw.write(transactionItem);
								bw.newLine();
								bw.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}

							updateTransaction();
							lblTotal.setText("Total: " + total());

						} else {
							JOptionPane.showMessageDialog(null, "Not enough in inventory.");
						}
					}
				}
			}
		});
		btnAdd.setEnabled(false);
		btnAdd.setBounds(616, 358, 87, 21);
		frmMoralidadPlasticProducts.getContentPane().add(btnAdd);

		btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (tableTransaction.getSelectedRowCount() == 1) {
					int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this item?",
							"Confimration", JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						int row = tableTransaction.getSelectedRow();
						clearItem(row);
					}
				} else if (tableTransaction.getRowCount() == 0) {
					JOptionPane.showMessageDialog(null, "The table is empty.");
				} else if (tableTransaction.getSelectedRowCount() == 0) {
					JOptionPane.showMessageDialog(null, "Select one row to delete.");
				} else if (tableTransaction.getSelectedRowCount() > 0) {
					int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete these items?",
							"Confimration", JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						while (tableTransaction.getSelectedRowCount() != 0) {
							int row = tableTransaction.getSelectedRow();
							clearItem(row);
						}
					}
				}
				lblTotal.setText("Total: " + total());
			}
		});
		btnDelete.setEnabled(false);
		btnDelete.setBounds(403, 459, 98, 21);
		panelTransaction.add(btnDelete);

		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				now = LocalDateTime.now();
				formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
				formattedDateTime = now.format(formatter);
				transactionFile = new File(formattedDateTime + ".txt");
			}

		});
		btnSave.setEnabled(false);
		btnSave.setBounds(403, 490, 98, 21);
		panelTransaction.add(btnSave);

		btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear these items?", "Confirmation", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					while (tableTransaction.getRowCount() != 0) {
						int row = 0;
						clearItem(row);
					}
				}
				lblTotal.setText("Total: " + total());
			}
		});
		btnClear.setEnabled(false);
		btnClear.setBounds(295, 490, 98, 21);
		panelTransaction.add(btnClear);

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to cancel?", "Confirmation",
						JOptionPane.YES_NO_OPTION);

				if (result == JOptionPane.YES_OPTION) {

					try {

						FileWriter fw = new FileWriter(transactionInfo);

						JOptionPane.showMessageDialog(null, "Transaction has been cancelled.");
						btnNew.setEnabled(true);
						btnAdd.setEnabled(false);
						btnSave.setEnabled(false);
						btnClear.setEnabled(false);
						btnDelete.setEnabled(false);
						btnCancel.setEnabled(false);

						updateTransaction();

					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnCancel.setEnabled(false);
		btnCancel.setBounds(351, 521, 98, 21);
		panelTransaction.add(btnCancel);

		btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				btnAdd.setEnabled(true);
				btnSave.setEnabled(true);
				btnNew.setEnabled(false);
				btnClear.setEnabled(true);
				btnDelete.setEnabled(true);
				btnCancel.setEnabled(true);

				JOptionPane.showMessageDialog(null, "Starting new transaction.");
			}
		});
		btnNew.setBounds(295, 459, 98, 21);
		panelTransaction.add(btnNew);

		btnInAdd = new JButton("Add");
		btnInAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddPanel ap = new AddPanel();
				ap.show();
				ap.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						updateInventory();
					}
				});
			}
		});
		btnInAdd.setBounds(20, 519, 98, 21);
		panelInventory.add(btnInAdd);

		btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (tableInventory.getSelectedRowCount() == 0 || tableInventory.getSelectedRowCount() > 1) {
					JOptionPane.showMessageDialog(null, "Select one row to edit.");
				} else {

					int row = tableInventory.getSelectedRow();
					item = model_inventory.getValueAt(row, 0).toString();
					unit = model_inventory.getValueAt(row, 1).toString();
					quantity = model_inventory.getValueAt(row, 2).toString();
					unitPrice = model_inventory.getValueAt(row, 3).toString();
					String originalLine = findLine(inventoryInfo, tableInventory.getSelectedRow() + 1);

					if (item.equals("") || unit.equals("") || quantity.equals("") || unitPrice.equals("")) {
						JOptionPane.showMessageDialog(null, "Please fill up all areas.");
						updateInventory();
					} else if (!quantity.matches("-?\\d+") || !unitPrice.matches("-?\\d+")) {
						JOptionPane.showMessageDialog(null, "Invalid input. Please try again.");
						updateInventory();
					} else {
						int result = JOptionPane.showConfirmDialog(null, "Edit this item?", "Confirmation",
								JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.YES_OPTION) {
							edit(inventoryInfo, originalLine, modifyLine());
						} else {
							updateInventory();
						}
					}
				}
			}
		});
		btnEdit.setBounds(128, 519, 98, 21);
		panelInventory.add(btnEdit);

		btnInDelete = new JButton("Delete");
		btnInDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tableInventory.getSelectedRowCount() == 1) {
					int result = JOptionPane.showConfirmDialog(null, "Delete this item?", "Confirmation",
							JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						delete(inventoryInfo, modifyLine());
						model_inventory.removeRow(tableInventory.getSelectedRow());
					}
				} else if (tableInventory.getRowCount() == 0) {
					JOptionPane.showMessageDialog(null, "The table is empty.");
				} else if (tableInventory.getSelectedRowCount() == 0) {
					JOptionPane.showMessageDialog(null, "Select one row to delete.");
				} else {
					int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete these rows?",
							"Confirmation", JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						while (tableInventory.getSelectedRowCount() != 0) {
							delete(inventoryInfo, modifyLine());
							model_inventory.removeRow(tableInventory.getSelectedRow());
						}
					}
				}
			}
		});
		btnInDelete.setBounds(236, 519, 98, 21);
		panelInventory.add(btnInDelete);

		btnSearch = new JButton("Search");
		btnSearch.setBounds(446, 27, 98, 21);
		panelInventory.add(btnSearch);

		/**
		 * Initializing the table.
		 * -------------------------------------------------------------------------------------------------------
		 */

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(20, 30, 481, 414);
		panelTransaction.add(scrollPane_1);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(20, 57, 524, 452);
		panelInventory.add(scrollPane);

		tableTransaction = new JTable();
		model_transaction = new DefaultTableModel();
		Object[] column_transaction = { "Item", "Quantity", "Unit price", "Price" };
		model_transaction.setColumnIdentifiers(column_transaction);
		tableTransaction.setModel(model_transaction);
		scrollPane_1.setViewportView(tableTransaction);

		tableInventory = new JTable();
		model_inventory = new DefaultTableModel();
		Object[] column_inventory = { "Item", "Unit", "Quantity", "Price" };
		model_inventory.setColumnIdentifiers(column_inventory);
		tableInventory.setModel(model_inventory);
		scrollPane.setViewportView(tableInventory);

		/**
		 * btnTrial button for temporary testing.
		 * -------------------------------------------------------------------------------------------------------
		 */

		JButton btnTrial = new JButton("TrialButton");
		btnTrial.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateInventory();
				updateTransaction();
			}
		});
		btnTrial.setBounds(266, 653, 98, 21);
		frmMoralidadPlasticProducts.getContentPane().add(btnTrial);

		updateInventory();
		tableInventory.getColumnModel().getColumn(0).setPreferredWidth(250);
		tableTransaction.getColumnModel().getColumn(0).setPreferredWidth(175);
	}
}
