import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.Toolkit;

public class AddPanel extends JFrame {

	private JPanel contentPane;
	private JTextField textItem;
	private JTextField textUnit;
	private JTextField textQuantity;
	private JTextField textPrice;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AddPanel frame = new AddPanel();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public AddPanel() {
		setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\Stacey\\eclipse-workspace\\moralidadPlasticTPS\\MPP.png"));
		setResizable(false);
		setTitle("Add an item");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(400, 250, 260, 355);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblItem = new JLabel("Item");
		lblItem.setBounds(26, 36, 100, 13);
		contentPane.add(lblItem);
		
		textItem = new JTextField();
		textItem.setBounds(26, 57, 181, 19);
		contentPane.add(textItem);
		textItem.setColumns(10);
		
		textUnit = new JTextField();
		textUnit.setColumns(10);
		textUnit.setBounds(26, 107, 181, 19);
		contentPane.add(textUnit);
		
		JLabel lblUnit = new JLabel("Unit");
		lblUnit.setBounds(26, 86, 100, 13);
		contentPane.add(lblUnit);
		
		textQuantity = new JTextField();
		textQuantity.setColumns(10);
		textQuantity.setBounds(26, 157, 181, 19);
		contentPane.add(textQuantity);
		
		JLabel lblQuantity = new JLabel("Quantity");
		lblQuantity.setBounds(26, 136, 100, 13);
		contentPane.add(lblQuantity);
		
		textPrice = new JTextField();
		textPrice.setColumns(10);
		textPrice.setBounds(26, 207, 181, 19);
		contentPane.add(textPrice);
		
		JLabel lblPrice = new JLabel("Price");
		lblPrice.setBounds(26, 186, 100, 13);
		contentPane.add(lblPrice);
		
		JButton btnNewButton = new JButton("Add");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					FileWriter fw = new FileWriter("inventoryInfo.txt", true);
					BufferedWriter bw = new BufferedWriter(fw);
					
					String item = textItem.getText();
					String unit = textUnit.getText();
					String quantity = textQuantity.getText();
					String price = textPrice.getText();
					
					if (item.equals("") || unit.equals("") || quantity.equals("") || price.equals("")) {
						JOptionPane.showMessageDialog(null, "Please fill up all areas.");
					} else if (!quantity.matches("-?\\d+") || !price.matches("-?\\d+")) {
						JOptionPane.showMessageDialog(null, "Invalid input. Please try again.");
					} else {
						bw.write(item + "/" + unit + "/" + quantity + "/" + price);
						bw.newLine();
						bw.close();
						JOptionPane.showMessageDialog(null, "Item added successfully.");
						textItem.setText("");
						textUnit.setText("");
						textQuantity.setText("");
						textPrice.setText("");
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnNewButton.setBounds(64, 248, 113, 21);
		contentPane.add(btnNewButton);
	}
}
