import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Toolkit;

public class Main extends JFrame {

	String myblack = "black1.png";
	String mywhite = "white1.png";
	private JPanel contentPane;
	private JTextField nametext;
    private OmokClient client=new OmokClient("���� ����");
    
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main frame = new Main();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Main() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 400, 300); //setBounds(100, 100, 400, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		// �ڹ� ������ ����
		setUndecorated(true);
		setLocationRelativeTo(null);
		
		// ���α׷� ���� ��ư
		JLabel close = new JLabel("");
		close.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {
				close.setIcon(new javax.swing.ImageIcon(getClass().getResource("close-hover.png")));
			}
			public void mouseExited(MouseEvent arg0) {
				close.setIcon(new javax.swing.ImageIcon(getClass().getResource("close.png")));
			}
			public void mouseClicked(MouseEvent arg0){
				System.exit(0);
			}
		});
		close.setIcon(new ImageIcon(Main.class.getResource("close.png")));
		close.setBounds(365, 7, 28, 30);
		contentPane.add(close);
		
		///////////////
		
		nametext = new JTextField();
		nametext.setFont(new Font("���� ���", Font.BOLD, 15));
		nametext.setBounds(202, 119, 149, 35);
		contentPane.add(nametext);
		nametext.setColumns(10);
		
		// �����ϱ� ��ư
		JLabel join = new JLabel("");
		join.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {
				join.setIcon(new javax.swing.ImageIcon(getClass().getResource("join-hover.png")));
			}
			public void mouseExited(MouseEvent arg0) {
				join.setIcon(new javax.swing.ImageIcon(getClass().getResource("join.png")));
			}
			public void mouseClicked(MouseEvent arg0){
				client.setSize(900, 740);
				client.setBounds(300, 50, 900, 740);
			    client.setVisible(false);
			    client.connect();
				client.nameBox.setText(nametext.getText());
				client.goToWaitRoom();
				client.setVisible(true);
				setVisible(false);
			}
		});
		join.setIcon(new ImageIcon(Main.class.getResource("join.png")));
		join.setBounds(107, 194, 179, 63);
		contentPane.add(join);

		JLabel background_d = new JLabel("");
		background_d.setIcon(new ImageIcon(Main.class.getResource("background_d2.png")));
		background_d.setBounds(0, 0, 400, 300);
		contentPane.add(background_d);
		
	    join.setVisible(true);
	    nametext.setVisible(true);
	    background_d.setVisible(true);
		//////////////////////
	}
}
