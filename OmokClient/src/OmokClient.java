import java.awt.BorderLayout;
//import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
//import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JButton;

class OmokBoard extends Canvas{
  	
	// �ٵϵ��� �������� ������ �� �ֵ��� �Ѵ�.
	// �⺻ ������ black1�� white1 �̹����̴�.
	String myblack = OmokClient.myblack;
	String mywhite = OmokClient.mywhite;
	// true�̸� ����ڰ� ���� ���� �� �ִ� ���¸� �ǹ��ϰ�,
	// false�̸� ����ڰ� ���� ���� �� ���� ���¸� �ǹ��Ѵ�.
	private boolean enable=false;
	private boolean running=false; // ������ ���� ���ΰ��� ��Ÿ���� ����
	private PrintWriter writer; // ������� �޽����� �����ϱ� ���� ��Ʈ��
	private Graphics gboard, gbuff; // ĵ������ ���۸� ���� �׷��Ƚ� ��ü
	private Image buff; // ���� ���۸��� ���� ����
	
	// �������� �����ϴ� Ŭ����
	public static final int BLACK = 1,WHITE = -1; // ��� ���� ��Ÿ���� ���
	private int[][]map; // ������ �迭
	private int size; // size�� ������ ���� �Ǵ� ���� ����, 15�� ���Ѵ�.
	private int cell; // ������ ũ��(pixel)
	private String info="[ ������ ������ �����Դϴ�. ]"; // ������ ���� ��Ȳ�� ��Ÿ���� ���ڿ�
	private int color=BLACK; // ������� �� ����

	OmokBoard(int s, int c) { // �������� ������(s=15, c=30)
		this.size = s; this.cell = c;
		map = new int[size+2][]; // ���� ũ�⸦ ���Ѵ�.
		for(int i=0;i < map.length;i++)
			map[i]=new int[size+2];
		setSize(size*(cell+2)+size, size*(cell+2)+size);    // �������� ũ�⸦ ����Ѵ�.
		/*���⼭���� ���콺�� ������ ������ ���� ���� ���� ���
		 */
		// �������� ���콺 �̺�Ʈ ó��
		addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent me){     // ���콺�� ������
				if(!enable)return;            // ����ڰ� ���� �� ���� �����̸� ���� ���´�.
				// ���콺�� ��ǥ�� map ��ǥ�� ����Ѵ�.
				int x=(int)Math.round(me.getX()/(double)cell);
				int y=(int)Math.round(me.getY()/(double)cell);
				// ���� ���� �� �ִ� ��ǥ�� �ƴϸ� ���� ���´�.
				if(x==0 || y==0 || x==size+1 || y==size+1)return;
				// �ش� ��ǥ�� �ٸ� ���� ������ ������ ���� ���´�.
				if(map[x][y]==BLACK || map[x][y]==WHITE)return;
				// ������� ���� ���� ��ǥ�� �����Ѵ�.
				writer.println("[STONE]" + x + " "+y);
				map[x][y]=color;
				repaint(); // �������� �׸���.
				// �̰���� �˻��Ѵ�.
				if(check(new Point(x, y), color)){
					OmokClient.msgView.append("�¸��Ͽ����ϴ�.\n");
					writer.println("[WIN]");
				}
				else OmokClient.msgView.append("��ٸ�����.\n");
				
				// ����ڰ� �� �� ���� ���·� �����.
				// ������� �θ� enable�� true�� �Ǿ� ����ڰ� �� �� �ְ� �ȴ�.
				enable=false;
			}
		});
	}
	
	public boolean isRunning(){           // ������ ���� ���¸� ��ȯ�Ѵ�.
		return running;
	}
	
	public void startGame(String col){     // ������ �����Ѵ�.
		running=true;
		if(col.equals("BLACK")){              // ���� ���õǾ��� ��
			enable=true; color=BLACK;
			OmokClient.msgView.append("�����Դϴ�.\n");
		}   
		else{                                // ���� ���õǾ��� ��
			enable=false; color=WHITE;
			OmokClient.msgView.append("��ٸ�����.\n");
		}
	}

	public void stopGame(){              // ������ �����.
		reset();                              // �������� �ʱ�ȭ�Ѵ�.
		writer.println("[STOPGAME]");        // ������� �޽����� ������.
		enable=false;
		running=false;
	}

	public void putOpponent(int x, int y){       // ������� ���� ���´�.
		map[x][y]=-color;
		OmokClient.msgView.append("��밡 �ξ����ϴ�.\n");
		repaint();
	}

	public void setEnable(boolean enable){
		this.enable=enable;
	}
	
	public void setWriter(PrintWriter writer){
		this.writer=writer;
	}

	/* ���⼭���� �׸��� ���� ��
	*/
	public void update(Graphics g){        // repaint�� ȣ���ϸ� �ڵ����� ȣ��ȴ�.
		paint(g);                             // paint�� ȣ���Ѵ�.
	}
	
	// paint�� ��� �׸��� ���
	public void paint(Graphics g){                // ȭ���� �׸���.
		if(gbuff==null){                             // ���۰� ������ ���۸� �����.
			buff=createImage(getWidth(),getHeight());
			gbuff=buff.getGraphics();
		}
		try {
			drawBoard(g);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    // �������� �׸���.
	}

	public void reset(){                         // �������� �ʱ�ȭ��Ų��.
		for(int i=0;i<map.length;i++)
			for(int j=0;j<map[i].length;j++)
				map[i][j]=0;
		OmokClient.msgView.append("������ ������ �����Դϴ�.\n");
		repaint();
	}

	private void drawLine() throws IOException{                     // �����ǿ� ���� �ߴ´�.
		gbuff.setColor(Color.black);
		BufferedImage image;
		image = ImageIO.read(getClass().getResourceAsStream("board.png"));
		gbuff.drawImage(image, 0,  0, null);
		for(int i=1; i<=size + 1;i++){
			gbuff.drawLine(cell - cell/2, i*cell - cell/2, cell*size + cell/2, i*cell - cell/2);
			gbuff.drawLine(i*cell - cell/2 , cell - cell/2, i*cell- cell/2 , cell*size + cell/2);
		}
	}

	private void drawBlack(int x, int y) throws IOException{         // �� ���� (x, y)�� �׸���.
		String myblack = OmokClient.myblack;
		Graphics2D gbuff=(Graphics2D)this.gbuff;
		BufferedImage image;
		image = ImageIO.read(getClass().getResourceAsStream(myblack));
		gbuff.drawImage(image, x*cell-cell/2,  y*cell-cell/2, null);
	}

	private void drawWhite(int x, int y) throws IOException{         // �� ���� (x, y)�� �׸���.
		String mywhite = OmokClient.mywhite;
		Graphics2D gbuff=(Graphics2D)this.gbuff;
		BufferedImage image;
		image = ImageIO.read(getClass().getResourceAsStream(mywhite));
		gbuff.drawImage(image, x*cell-cell/2,  y*cell-cell/2, null);
	}

	private void drawStones() throws IOException{                  // map ������ ������ ��� �׸���.
		for(int x=1; x<=size;x++)
			for(int y=1; y<=size;y++){
				if(map[x][y]==BLACK)
					drawBlack(x, y);
				else if(map[x][y]==WHITE)
					drawWhite(x, y);
			}
	}

	synchronized private void drawBoard(Graphics g) throws IOException{      // �������� �׸���.
		// ���ۿ� ���� �׸��� ������ �̹����� �����ǿ� �׸���.
		gbuff.clearRect(0, 0, getWidth(), getHeight());
		drawLine();
		drawStones();
		gbuff.setColor(Color.red);
		g.drawImage(buff, 0, 0, this);
	}

	/*
	 * ���⼭���� �¸� ������ ���� ��
	 */
	private boolean check(Point p, int col){
		if(count(p, 1, 0, col)+count(p, -1, 0, col)==4)
			return true;
		if(count(p, 0, 1, col)+count(p, 0, -1, col)==4)
			return true;
		if(count(p, -1, -1, col)+count(p, 1, 1, col)==4)
			return true;
		if(count(p, 1, -1, col)+count(p, -1, 1, col)==4)
			return true;
		return false;
	}

	private int count(Point p, int dx, int dy, int col){
		int i=0;
		for(; map[p.x+(i+1)*dx][p.y+(i+1)*dy]==col ;i++);
		return i;
	}
}  // OmokBoard ���� ��

// �̰��� ��¥ Ŭ�����̰� OmokBoard�� �̿��ϴ� ��ü�� �ȴ�.
public class OmokClient extends JFrame implements Runnable, ActionListener {
	
	public static String myblack = "black1.png";
	public static String mywhite = "white1.png";
	/*
	 * ���⼭���� ���� �����ΰ� ������ �κ�
	 */
	public static TextArea msgView=new TextArea("", 1,1,1); // �޽����� �����ִ� ����
	private TextField sendBox=new TextField(""); // ���� �޽����� ���� ����
	private TextField roomName = new TextField("");
	private TextField roomPassword = new TextField("");
	public static TextField nameBox=new TextField(); // ����� �̸� ����
	//private TextField roomBox=new TextField("0"); // �� ��ȣ ����

	// �濡 ������ �ο��� ���� �����ִ� ���̺�
	private Label pInfo=new Label("����:  ��");
	private java.awt.List pList=new java.awt.List();  // ����� ����� �����ִ� ����Ʈ
	private java.awt.List rList = new java.awt.List(); //�� ����� �����ִ� ����Ʈ
	JLabel makeRoom = new JLabel("");
	JLabel enterButton = new JLabel("");
	JLabel exitButton = new JLabel("");
	// ���� ������ �����ִ� ���̺�
	private Label infoView=new Label("���� ���ӿ� �� ���� ȯ���մϴ�.", 1);
	private OmokBoard board=new OmokBoard(15,30); // ������ ��ü(�̰� ���� ��� ���� ���) //private OmokBoard board=new OmokBoard(15,50);
	private BufferedReader reader; // �Է� ��Ʈ��
	private PrintWriter writer; // ��� ��Ʈ��
	private Socket socket; // ����
	private int roomNumber=-1; // �� ��ȣ
	private String userName=null; // ����� �̸�

	Panel p1=new Panel();
	Panel p2=new Panel();
	Panel p3 = new Panel();
	
	JFrame f1 = new JFrame("Create Room");
	

	JLabel ready = new JLabel("");
	JLabel quit = new JLabel("");
	JLabel readyback = new JLabel("");
	JLabel battleground = new JLabel("");
	
	public OmokClient(String title){ // ������
		super(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);   
		infoView.setFont(new Font("���� ���", Font.BOLD, 20));
		infoView.setBounds(500,281,350,40);
		infoView.setBackground(new Color(51, 51, 255));
		nameBox.setBackground(Color.WHITE);
		nameBox.setForeground(Color.BLACK);
		nameBox.setFont(new Font("��ü�� ����ü", Font.BOLD, 30));
		nameBox.setBounds(175, 125, 190, 40); //nameBox.setBounds(165, 161, 214, 58);
		getContentPane().add(nameBox);
		//roomBox.setFont(new Font("���� ���", Font.BOLD, 30));
		//roomBox.setBounds(550, 125, 190, 40); //roomBox.setBounds(680, 161, 300, 58);
		//getContentPane().add(roomBox);
		getContentPane().add(infoView);
		Toolkit toolkit = getToolkit();
		Dimension size = toolkit.getScreenSize();
		setLocation(size.width/2 - 640, size.height/2 - 200);
		infoView.hide();
		// �ٷ� �ؿ� �ִ� �г�
		
		//p1.setBackground(new Color(51, 51, 255)); //p2.setBackground(new Color(50, 205, 50));
		p1.setLayout(new BorderLayout());
		p1.add(rList, BorderLayout.CENTER); //p1.add(pList,BorderLayout.CENTER); 
		p1.setBounds(55,300,380,300); //p2.setBounds(55,340,1170,300);

		p2.setLayout(new BorderLayout());
		p2.add(msgView, BorderLayout.CENTER); //p3.add(msgView, BorderLayout.CENTER);
		// ���� ������Ʈ�� �����ϰ� ��ġ�Ѵ�.
		msgView.setEditable(false);
		sendBox.setFont(new Font("���� ���", Font.BOLD, 15)); //sendBox.setFont(new Font("���� ���", Font.BOLD, 30));
		p2.add(sendBox, "South");  //p3.add(sendBox, "North");
		p2.setBounds(450, 300, 380, 300); //p3.setBounds(55, 660, 1170,250);
		
		p3.setLayout(new BorderLayout());
		//p3.setBackground(new Color(0, 102, 255));	
		//getContentPane().add(roomName);
		//roomName.hide();
		//roomPassword.hide();
		//getContentPane().add(roomPassword);
		
		p3.setBounds(250, 250, 400, 300);
		p3.hide();	
		
		
		
		
		enterButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				enterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("go.png")));
			}
			public void mouseExited(MouseEvent arg0) {
				enterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("enter1.png")));
			}
			public void mouseClicked(MouseEvent arg0) {
				try {
					writer.println("[ROOMNAME]" + roomName.getText());
					//writer.println("[ENTER]" + userName.getText());
					//writer.println("[ROOMPASSWORD]" + Integer.parseInt(roomPassword.getText()));
					roomName.hide();
					roomPassword.hide();
					makeRoom.hide();
					nameBox.hide();
					pInfo.hide();
					enterButton.hide();
					board.show();
					ready.show();
					quit.show();
					readyback.show();
					battleground.show();
					infoView.show();
					exitButton.show();
					p2.setBounds(500, 320, 350, 300);
					p2.show();
					enterButton.hide();
					p3.hide();
					p1.hide();
					f1.setVisible(false);
				}catch(Exception ie) {
					
				}
			}
		});
		enterButton.setIcon(new ImageIcon(Main.class.getResource("enter1.png")));
		
		
		
		makeRoom.setBounds(700, 205, 130, 46); //enterButton2.setBounds(1000, 128, 233, 120);
		getContentPane().add(makeRoom);
		makeRoom.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {
				makeRoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_enter.jpg")));
			}
			public void mouseExited(MouseEvent arg0) {
				makeRoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_enter.jpg")));
			}
			public void mouseClicked(MouseEvent arg0){
				try{
					msgView.setText("");
					/*if(Integer.parseInt(roomBox.getText())<1){
						infoView.setText("���ȣ�� �߸��Ǿ����ϴ�. 1�̻�");
						return;
					}*/
					//writer.println("[ROOM]"+Integer.parseInt(roomBox.getText()));
					f1.add(enterButton);
					f1.add(roomName);
					f1.add(roomPassword);
					f1.setBounds(550, 300, 400, 300);
					roomName.setBounds(100, 100, 190, 30);
					roomPassword.setBounds(100, 140, 190, 30);
					enterButton.setBounds(310, 230, 60, 21);
					f1.setLayout(null);
					f1.setLayout(null);
					f1.setVisible(true);
				}catch(Exception ie){
					infoView.setText("�Է��Ͻ� ���׿� ������ �ҽ��ϴ�.");
				}
			}
		});	
		makeRoom.setIcon(new ImageIcon(Main.class.getResource("hs_enter.jpg")));
		
		
		exitButton.setBounds(500, 209, 421, 68);
		getContentPane().add(exitButton);
		exitButton.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {
				exitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("toroom-hover.png")));
			}
			public void mouseExited(MouseEvent arg0) {
				exitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("torooom.png")));
			}
			public void mouseClicked(MouseEvent arg0){
				msgView.setText("");
				goToWaitRoom();
				board.hide();
				makeRoom.show();
				ready.hide();
				quit.hide();
				nameBox.show();
				readyback.show();
				battleground.hide();
				pInfo.show();
				p2.show();
				p2.setBounds(450, 300, 380, 300); //p3.setBounds(55, 660, 1170,250);
				p1.show();
				infoView.hide();
				exitButton.hide();
				
			}
		});
		exitButton.setIcon(new ImageIcon(Main.class.getResource("torooom.png")));
		exitButton.hide();
		

		ready.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {
				ready.setIcon(new javax.swing.ImageIcon(getClass().getResource("go-hover.png")));
			}
			public void mouseExited(MouseEvent arg0) {
				ready.setIcon(new javax.swing.ImageIcon(getClass().getResource("go.png")));
			}
			public void mouseClicked(MouseEvent arg0){
				try{
					writer.println("[START]");
					infoView.setText("����� ������ ��ٸ��ϴ�.");
				}catch(Exception e){}
			}
		});
		ready.setIcon(new ImageIcon(Main.class.getResource("go.png")));
		ready.setBounds(500, 128, 209, 68);
		getContentPane().add(ready);
		ready.hide();
		
	
		quit.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {
				quit.setIcon(new javax.swing.ImageIcon(getClass().getResource("no-hover.png")));
			}
			public void mouseExited(MouseEvent arg0) {
				quit.setIcon(new javax.swing.ImageIcon(getClass().getResource("no.png")));
			}
			public void mouseClicked(MouseEvent arg0){
				try{
					writer.println("[DROPGAME]");
					endGame("����Ͽ����ϴ�.");
				}catch(Exception e){}
			}
		});
		quit.setIcon(new ImageIcon(Main.class.getResource("no.png")));
		quit.setBounds(700, 128, 209, 68);
		getContentPane().add(quit);
		quit.hide();
		
		
		
		
		pInfo.setBackground(new Color(51, 51, 255)); //pInfo.setBackground(new Color(50, 205, 50));
		pInfo.setFont(new Font("���� ���", Font.BOLD, 30));
		pInfo.setBounds(52, 190, 800, 70); //pInfo.setBounds(55, 250, 1170, 60);
		getContentPane().add(pInfo);
		board.setBounds(20, 110, 482, 476);
		getContentPane().add(board);
		board.hide();getContentPane().add(p1);getContentPane().add(p2);
		
		 	
 
		
		readyback.setIcon(new ImageIcon(Main.class.getResource("HS_robby.jpg")));
		readyback.setBounds(0, 0, 900, 703);
		getContentPane().add(readyback);
		
		
		battleground.setIcon(new ImageIcon(Main.class.getResource("battleground.png")));
		battleground.setBounds(0, 0, 1280, 1000);
		getContentPane().add(battleground);
		battleground.hide();
		
		Panel p2_1=new Panel();
		p2_1.setBounds(2000, 0, 1170, 35);
		getContentPane().add(p2_1);
		 
		// �̺�Ʈ �����ʸ� ����Ѵ�.
		sendBox.addActionListener(this);
		// ������ �ݱ� ó��
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we){
				System.exit(0);
			}
		});
	}

	/*
	 * ���⼭���� ������ �޽����� ������ �κ�
	 * (�濡 ���ٴ��� ���̵� �Է��Ѵٴ��� ��)
	 */
	// ������Ʈ���� �׼� �̺�Ʈ ó��
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource()==sendBox){             // �޽��� �Է� �����̸�
			String msg=sendBox.getText();
			if(msg.length()==0)
				return;
			if(msg.length()>=30)msg=msg.substring(0,30);
			try{  
				writer.println("[MSG]"+msg);
				sendBox.setText("");
			}catch(Exception ie){}
		}
		
		else if(ae.getSource()==makeRoom){         // �����ϱ� ��ư�̸�

		}

		else if(ae.getSource()==exitButton){           // ���Ƿ� ��ư�̸�
			try{
				goToWaitRoom();
			}catch(Exception e){}
		}
	}

	/* ���Ƿ� �����ϴ� �κ���.
		���⼭ ó���ϴ� �κ��� ���� �̸��� �Է��ϰ� �����ϴ� �κ�(���� ù ��° �κ�)
	*/
	void goToWaitRoom(){                   // ���Ƿ� ��ư�� ������ ȣ��ȴ�.
		if(userName==null){
			String name=nameBox.getText();
			if(name.length()<=1 || name.length()>10){
				infoView.setText("�̸��� 2�ں��� 10�� ���̸� �����մϴ�.");
				nameBox.requestFocus();
				return;
			}
			userName=name;
			writer.println("[NAME]"+userName);    
			nameBox.setText(userName);
			nameBox.setEditable(false);
		}  
		msgView.setText("");
		// ������ ROOM �߿��� 0�� �ش��Ѵ�.
		writer.println("[ROOM]0");
		infoView.setText("���ǿ� �����ϼ̽��ϴ�.");
		//roomBox.setText("0");
		enterButton.setEnabled(true);
		exitButton.setEnabled(false);
	}
 
	public void run(){
		String msg;                             // �����κ����� �޽���
		try{
			while((msg=reader.readLine())!=null){
				/*
				 * �̰� ���� ��ǥ�� �����ؼ� �º� ������ ���� �����ϴ� 
				 */
				if(msg.startsWith("[STONE]")){     // ������� ���� ���� ��ǥ
					String temp=msg.substring(7);
					int x=Integer.parseInt(temp.substring(0,temp.indexOf(" ")));
					int y=Integer.parseInt(temp.substring(temp.indexOf(" ")+1));
					board.putOpponent(x, y);     // ������� ���� �׸���.
					board.setEnable(true);        // ����ڰ� ���� ���� �� �ֵ��� �Ѵ�.
				}
				/*����� �濡 �����ϴ� �Ͱ� ������ �Ϳ� ������ ��
				 * 
				 */
				else if(msg.startsWith("[ROOM]")){    // �濡 ����
					if(!msg.equals("[ROOM]0")){          // ������ �ƴ� ���̸�
						enterButton.setEnabled(false);
						exitButton.setEnabled(true);
						infoView.setText(msg.substring(6)+"�� �濡 �����ϼ̽��ϴ�.");
					}
					else infoView.setText("���ǿ� �����ϼ̽��ϴ�.");
					roomNumber=Integer.parseInt(msg.substring(6));     // �� ��ȣ ����
					if(board.isRunning()){                    // ������ �������� �����̸�
						board.stopGame();                    // ������ ������Ų��.
					}
				}
				else if(msg.startsWith("[FULL]")){       // ���� �� �����̸�
					infoView.setText("���� ���� ������ �� �����ϴ�.");
				}
				else if(msg.startsWith("[PLAYERS]")){      // �濡 �ִ� ����� ���
					nameList(msg.substring(9));
				}
				else if(msg.startsWith("[ENTER]")){        // �մ� ����
					pList.add(msg.substring(7));
					playersInfo();
					msgView.append("["+ msg.substring(7)+"]���� �����Ͽ����ϴ�.\n");
				}
				else if(msg.startsWith("[ROOMENTER]")) {
					rList.add(msg.substring(11));
				}
				else if(msg.startsWith("[EXIT]")){          // �մ� ����
					pList.remove(msg.substring(6));// ����Ʈ���� ����
					//rList.remove(msg.substring(6));
					playersInfo();                        // �ο����� �ٽ� ����Ͽ� �����ش�.
					msgView.append("["+msg.substring(6)+"]���� �ٸ� ������ �����Ͽ����ϴ�.\n");
					endGame("��밡 �������ϴ�.");
				}
				else if(msg.startsWith("[DISCONNECT]")){     // �մ� ���� ����
					pList.remove(msg.substring(12));
					playersInfo();
					msgView.append("["+msg.substring(12)+"]���� ������ �������ϴ�.\n");
					if(roomNumber!=0)
						endGame("��밡 �������ϴ�.");
				}
				/*����� ���� ����� ������ �κ�
				 * 
				 */
				else if(msg.startsWith("[COLOR]")){          // ���� ���� �ο��޴´�.
					String color=msg.substring(7);
					board.startGame(color);                      // ������ �����Ѵ�.
					if(color.equals("BLACK"))
						infoView.setText("�浹�� ��ҽ��ϴ�.");
					else
						infoView.setText("�鵹�� ��ҽ��ϴ�.");              // ��� ��ư Ȱ��ȭ
				}
				else if(msg.startsWith("[DROPGAME]"))      // ��밡 ����ϸ�
					endGame("��밡 ����Ͽ����ϴ�.");
				else if(msg.startsWith("[WIN]"))              // �̰�����
					endGame("�̰���ϴ�.");
				else if(msg.startsWith("[LOSE]"))            // ������
					endGame("�����ϴ�.");
				// ��ӵ� �޽����� �ƴϸ� �޽��� ������ �����ش�.
				else msgView.append(msg+"\n");
			}
		}catch(IOException ie){
			msgView.append(ie+"\n");
		}
		msgView.append("������ ������ϴ�.");
	}
	
	private void endGame(String msg){                // ������ �����Ű�� �޼ҵ�
		infoView.setText(msg);
		try{ Thread.sleep(2000); }catch(Exception e){}    // 2�ʰ� ���
		if(board.isRunning())board.stopGame();
	}
	
	private void playersInfo(){                 // �濡 �ִ� �������� ���� �����ش�.
		int count=pList.getItemCount();
		if(roomNumber==0)
			pInfo.setText("����: "+count+"��"); //���� �ο� ��
		else pInfo.setText(roomNumber+" �� ��: "+count+"��");
		// �뱹 ���� ��ư�� Ȱ��ȭ ���¸� �����Ѵ�.
	}
	

	// ����� ����Ʈ���� ����ڵ��� �����Ͽ� pList�� �߰��Ѵ�.
	private void nameList(String msg){
		pList.removeAll();
		StringTokenizer st=new StringTokenizer(msg, "\t");
		while(st.hasMoreElements())
			pList.add(st.nextToken());
		playersInfo();
	}
	
	private void roomList(String msg) {
		rList.removeAll();
		StringTokenizer st = new StringTokenizer(msg, "\t");
		while(st.hasMoreTokens())
			rList.add(st.nextToken());
	}
	
	public void connect(){ // ����
		try{
			msgView.append("������ ������ ��û�մϴ�.\n");
			socket=new Socket("localhost", 9735);
			msgView.append("���ῡ �����Ͽ����ϴ�.\n");
			msgView.append("�̸��� �Է��ϰ� ���Ƿ� �����ϼ���.\n");
			reader=new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			writer=new PrintWriter(socket.getOutputStream(), true);
			new Thread(this).start();
			board.setWriter(writer);
		}catch(Exception e){
			msgView.append(e+"\n\n���ῡ �����Ͽ����ϴ�.\n");  
		}
	}

}