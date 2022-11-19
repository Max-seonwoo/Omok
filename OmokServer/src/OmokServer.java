import java.net.*;
import java.io.*;
import java.util.*;

// ������ ������ �����ϰ� ���ִ� Ŭ�����Դϴ�.
public class OmokServer implements Runnable{

	private ServerSocket server; // ���� ����
	private Massenger Man=new Massenger(); // �޽��� �߼���
	private Random rnd= new Random(); // ��� ���� �������� ��
	
	// ������ �����ϴ� �Լ�
	void startServer(){
		try{
			server=new ServerSocket(9735);
			Main.textArea.append("������ ���۽�ŵ�ϴ�.\n");
			while(true){
				// Ŭ���̾�Ʈ�� ����� ������ ȹ��
				Socket socket=server.accept();
				// �����带 ����� ����
				controller con=new controller(socket);
				con.start();
				// bMan�� �����带 �߰��Ѵ�.
				Man.add(con);
				Main.textArea.append("���� " + Man.size() + "���� ������ �ֽ��ϴ�.\n");
      }
    }catch(Exception e){
      System.out.println(e);
    }
  }
  
	// Ŭ���̾�Ʈ�� ����ϴ� ������ Ŭ����
	class controller extends Thread{
		private int roomNumber = -1;   // �� ��ȣ
		private String roomName = "";
		private String userName = null;       // ����� �̸�
		private Socket socket;              // ����
		// ���� �غ� ����, true�̸� ������ ������ �غ� �Ǿ����� �ǹ��Ѵ�.
		private boolean ready=false;
		private BufferedReader reader;     // �Է� ��Ʈ��
		private PrintWriter writer;           // ��� ��Ʈ��
		controller(Socket socket){     // ������
			this.socket=socket;
		}
		Socket getSocket(){               // ������ ��ȯ�Ѵ�.
			return socket;
		}
		int getRoomNumber(){             // �� ��ȣ�� ��ȯ�Ѵ�.
			return roomNumber;
		}
		String getRoomName() {
			return roomName;
		}
		String getUserName(){             // ����� �̸��� ��ȯ�Ѵ�.
			return userName;
		}
		boolean isReady(){                 // �غ� ���¸� ��ȯ�Ѵ�.
			return ready;
		}
		public void run(){
			try{
				reader=new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
				writer=new PrintWriter(socket.getOutputStream(), true);
				String msg;                     // Ŭ���̾�Ʈ�� �޽���
				while((msg=reader.readLine())!=null){
					// msg�� "[NAME]"���� ���۵Ǵ� �޽����̸�
					if(msg.startsWith("[NAME]")){
						userName=msg.substring(6);          // userName�� ���Ѵ�.
					}
					// msg�� "[ROOM]"���� ���۵Ǹ� �� ��ȣ�� ���Ѵ�.
					/*else if(msg.startsWith("[ROOM]")){
						int roomNum=Integer.parseInt(msg.substring(6));
						if(!Man.isFull(roomNum)){             // ���� �� ���°� �ƴϸ�
							// ���� ���� �ٸ� ��뿡�� ������� ������ �˸���.
							if(roomNumber!=-1)
								Man.sendToOthers(this, "[EXIT]"+userName);
							// ������� �� �� ��ȣ�� �����Ѵ�.
							roomNumber=roomNum;
							// ����ڿ��� �޽����� �״�� �����Ͽ� ������ �� ������ �˸���.
							writer.println(msg);
							// ����ڿ��� �� �濡 �ִ� ����� �̸� ����Ʈ�� �����Ѵ�.
							writer.println(Man.getNamesInRoom(roomNumber));
							// �� �濡 �ִ� �ٸ� ����ڿ��� ������� ������ �˸���.
							Man.sendToOthers(this, "[ENTER]"+userName);
						}
						else writer.println("[FULL]");        // ����ڿ� ���� á���� �˸���.
					}*/
					
					else if(msg.startsWith("[ROOMNAME]")) {
						String roomname = msg.substring(10);
						if(!Man.isFull(roomname)) { 
							if(roomName != "") {
								Man.sendToOthers(this, "[EXIT]" + userName);
								//Man.sendToOthers(this, "[EXIT]" + roomName);
							}
							roomName = roomname;
							writer.println(msg);
							writer.println(Man.getNamesInRoom(roomName));
							
							Man.sendToOthers(this, "[ROOMENTER]" + roomName);
						}
						else 
							writer.println("[FULL]");
					}
					
					// "[STONE]" �޽����� ������� �����Ѵ�.
					else if(roomName!="" && msg.startsWith("[STONE]"))
						Man.sendToOthers(this, msg);
					// ��ȭ �޽����� �濡 �����Ѵ�.
					else if(msg.startsWith("[MSG]"))
						Man.sendToRoom(roomName, "["+userName+"]: "+msg.substring(5));
					// "[START]" �޽����̸�
					else if(msg.startsWith("[START]")){
						ready=true;   // ������ ������ �غ� �Ǿ���.
						// �ٸ� ����ڵ� ������ ������ �غ� �Ǿ�����
						if(Man.isReady(roomName)){
							// ��� ���� ���ϰ� ����ڿ� ������� �����Ѵ�.
							int a=rnd.nextInt(2); 
							if(a==0){
								writer.println("[COLOR]BLACK");
								Man.sendToOthers(this,"[COLOR]WHITE");
							}
							else{
								writer.println("[COLOR]WHITE");
								Man.sendToOthers(this,"[COLOR]BLACK");
							}
						}
					}
					// ����ڰ� ������ �����ϴ� �޽����� ������
					else if(msg.startsWith("[STOPGAME]"))
						ready=false;
					// ����ڰ� ������ ����ϴ� �޽����� ������
					else if(msg.startsWith("[DROPGAME]")){
						ready=false;
						// ������� ������� ����� �˸���.
						Man.sendToOthers(this, "[DROPGAME]");
					}
					// ����ڰ� �̰�ٴ� �޽����� ������
					else if(msg.startsWith("[WIN]")){
						ready=false;
						// ����ڿ��� �޽����� ������.
						writer.println("[WIN]");
						// ������� ������ �˸���.
						Man.sendToOthers(this, "[LOSE]");
					}  
				}
			}catch(Exception e){
			}finally{
				try{
					Man.remove(this);
					if(reader!=null) reader.close();
					if(writer!=null) writer.close();
					if(socket!=null) socket.close();
					reader=null; writer=null; socket=null;
					if(userName == null)
						userName = "�ſ� �Ҹ��� �����";
					Main.textArea.append(userName+"���� ������ �������ϴ�.\n");
					Main.textArea.append("���� " + Man.size() + "���� ������ �ֽ��ϴ�.\n");
					// ����ڰ� ������ �������� ���� �濡 �˸���.
					Man.sendToRoom(roomName,"[DISCONNECT]"+userName);
				}catch(Exception e){}
			}
		}
	}

	class Massenger extends Vector{       // �޽����� �����ϴ� Ŭ����
		void add(controller con){           // �����带 �߰��Ѵ�.
			super.add(con);
		}
		void remove(controller con){        // �����带 �����Ѵ�.
			super.remove(con);
		}
		controller getOT(int i){            // i��° �����带 ��ȯ�Ѵ�.
			return (controller)elementAt(i);
		}
		Socket getSocket(int i){              // i��° �������� ������ ��ȯ�Ѵ�.
			return getOT(i).getSocket();
		}
		// i��° ������� ����� Ŭ���̾�Ʈ���� �޽����� �����Ѵ�.
		void sendTo(int i, String msg){
			try{
				PrintWriter pw= new PrintWriter(getSocket(i).getOutputStream(), true);
				pw.println(msg);
			}catch(Exception e){}  
		}
		/*int getRoomNumber(int i){            // i��° �������� �� ��ȣ�� ��ȯ�Ѵ�.
			return getOT(i).getRoomNumber();
		}*/
		String getRoomName(int i) {
			return getOT(i).getRoomName();
		}
		/*synchronized boolean isFull(int roomNum){    // ���� á���� �˾ƺ���.
			if(roomNum==0)
				return false;                 // ������ ���� �ʴ´�.
			// �ٸ� ���� 2�� �̻� ������ �� ����.
			int count=0;
			for(int i=0;i<size();i++)
				if(roomNum==getRoomNumber(i))count++;
			if(count>=2)
				return true;
			return false;
		}*/
		
		synchronized boolean isFull(String roomname) {
			if(roomname == "")
				return false;
			int count = 0;
			for(int i = 0; i < size(); i++) 
				if(roomname.equals(getRoomName(i))) count++;
			if(count >= 2)
				return true;
			return false;
		}

		// roomNum �濡 msg�� �����Ѵ�.
		/*void sendToRoom(int roomNum, String msg){
			for(int i=0;i<size();i++)
				if(roomNum==getRoomNumber(i))
					sendTo(i, msg);
		}*/
		
		void sendToRoom(String roomname, String msg) {
			for(int i = 0; i < size(); i++) {
				if(roomname.equals(getRoomName(i)))
					sendTo(i, msg);
			}
		}
    
		// ot�� ���� �濡 �ִ� �ٸ� ����ڿ��� msg�� �����Ѵ�.
		/*void sendToOthers(controller ot, String msg){
			for(int i=0;i<size();i++)
				if(getRoomNumber(i)==ot.getRoomNumber() && getOT(i)!=ot)
					sendTo(i, msg);
		}*/
		
		void sendToOthers(controller ot, String msg){
			for(int i=0;i<size();i++)
				if(getRoomName(i).equals(ot.getRoomName()) && getOT(i)!=ot)
					sendTo(i, msg);
		}
    
		// ������ ������ �غ� �Ǿ��°��� ��ȯ�Ѵ�.
		// �� ���� ����� ��� �غ�� �����̸� true�� ��ȯ�Ѵ�.
		/*synchronized boolean isReady(int roomNum){
			int count=0;
			for(int i=0;i<size();i++)
				if(roomNum==getRoomNumber(i) && getOT(i).isReady())
					count++;
			if(count==2)
				return true;
			return false;
		}*/
		
		synchronized boolean isReady(String roomname){
			int count=0;
			for(int i=0;i<size();i++)
				if(roomname.equals(getRoomName(i)) && getOT(i).isReady())
					count++;
			if(count==2)
				return true;
			return false;
		}

		// roomNum�濡 �ִ� ����ڵ��� �̸��� ��ȯ�Ѵ�.
		/*String getNamesInRoom(int roomNum){
			StringBuffer sb=new StringBuffer("[PLAYERS]");
			for(int i=0;i<size();i++)
				if(roomNum==getRoomNumber(i))
					sb.append(getOT(i).getUserName()+"\t");
			return sb.toString();
		}*/
		String getNamesInRoom(String roomname) {
			StringBuffer sb = new StringBuffer("[PLAYERS]");
			for(int i = 0; i < size(); i++) {
				if(roomname.equals(getRoomName(i)))
					sb.append(getOT(i).getUserName() + "\t");
			}
			return sb.toString();
		}
	}
	
	@Override
	public void run() {
	    OmokServer server=new OmokServer();
	    server.startServer();
	}
	
	public void out() throws IOException {
		this.server.close();
	}
}