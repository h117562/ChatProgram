package chatServer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;



public class ServerSend extends JFrame implements ActionListener { // 0000, 0011
   // �ڵ� import ����Ű ctrl+shift+o �ڵ� ä��� ctrl+space
   private JPanel contentPane; // GUI Member 1011
   private JTextField port_tf;

   private JTextArea textArea = new JTextArea();
   private JButton start_btn = new JButton("���� ����"); // Refactor-->Rename
   private JButton stop_btn = new JButton("���� ����");

   // socket ���� ���� �κ� //2000
   private ServerSocket server_socket;
   private Socket socket;
   int port = 12345;

   // Stream ���� //3000

   // ��Ÿ ���� ���� //5000
   private Vector<UserInfo> user_vc = new Vector<UserInfo>();
   private Vector<RoomInfo> room_vc = new Vector<RoomInfo>();   //7000
   StringTokenizer st;                                 // 6000
   private boolean create_room_ok = true;                 //9800
   private boolean stop_server = false;                  //9800

   ServerSend() // ������ //1000
   {
      init(); // GUI �ʱ�ȭ, ȭ�� ���� //1000
      start(); // ������ ���� �޼ҵ� //1011
      //Server_start(); // 2000 8600

   }

   private void init() // 1000
   {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(30, 100, 321, 370);
      contentPane = new JPanel();
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);

      JLabel lblNewLabel_2 = new JLabel("��Ʈ ��ȣ");
      lblNewLabel_2.setBounds(12, 245, 57, 15);
      contentPane.add(lblNewLabel_2);

      port_tf = new JTextField();
      port_tf.setBounds(81, 242, 212, 21);
      contentPane.add(port_tf);
      port_tf.setColumns(10);

      start_btn.setBounds(12, 286, 138, 23);
      contentPane.add(start_btn);

      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setBounds(12, 10, 281, 210);
      contentPane.add(scrollPane);

      scrollPane.setViewportView(textArea);
      textArea.setEditable(false); // 6666 ȭ�� ����Ʈ ����

      stop_btn.setBounds(155, 286, 138, 23);
      contentPane.add(stop_btn);
      stop_btn.setEnabled(false);      //9100

      this.setVisible(true); // ȭ�� ���̱�
   }

   private void start() // 1011
   {
      start_btn.addActionListener(this);
      stop_btn.addActionListener(this);
   }

   private void Server_start() // 2000
   {
      try {
         port = 12345;
         port = Integer.parseInt(port_tf.getText().trim());
         server_socket = new ServerSocket(port); // 2200
      } catch (IOException e) {
         JOptionPane.showMessageDialog(null, "�̹� ������� ��Ʈ", "�˸�", JOptionPane.ERROR_MESSAGE); //9700
      }
      if (server_socket != null) {
         stop_server = false;                  //9800
         connection(); // 1000
      }
   }

   public void connection() { // 2000

      Thread th = new Thread(new Runnable() { // 2300
         public void run() {
            while (stop_server!=true) { // 4000 ������
               try {
                  textArea.append("����� ���� �����\n");
                  socket = server_socket.accept();
                  textArea.append("Ŭ���̾�Ʈ ���� �Ϸ�\n");
                  UserInfo user = new UserInfo(socket);
                  user.start();

               } catch (IOException e) {
                  
               }
            }

         }
      });
      th.start(); // 2300
   }

   class UserInfo extends Thread { // 4000
      // Stream ����
      private InputStream is;
      private DataInputStream dis;
      private OutputStream os;
      private DataOutputStream dos;
      private Socket clientSocket;
      private String userID = null;
      private String roomID = null;   //9810
      

      UserInfo(Socket socket) {
         this.clientSocket = socket;
         UserNetwork(); // 4200
      }

      private void UserNetwork() { // 4200
         try { // 3000
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);

            userID = dis.readUTF();
            textArea.append(userID + " Ŭ���̾�Ʈ ����\n");
            
            // ����Ǿ� �ִ� Ŭ���̾�Ʈ�鿡�� ������ ���� ����
            System.out.println("���� ������ Ŭ���̾�Ʈ �� = "+ user_vc.size());
            for (int i = 0; i < user_vc.size(); i++) {         //5100
               UserInfo u = (UserInfo) user_vc.elementAt(i);
               u.send_Msg("NewUser/"+ userID);
               System.out.println("SEND NewUser");
            }
            
            //���� ������ Ŭ���̾�Ʈ����                        //5100
            for(int i=0; i<user_vc.size();i++) {
               UserInfo u = (UserInfo) user_vc.elementAt(i);
               send_Msg("OldUser/"+u.userID);
            }
   
            // �ڽſ��� ������ ������ ä�� �� ���� ����               //7200
            for (int i = 0; i < room_vc.size(); i++) 
            {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               send_Msg("Old_Room/" + r.Room_name);
            }
            
            send_Msg("room_list_update/update");       //9300
            //���� ������ Ŭ���̾�Ʈ ���                  //5100
            user_vc.add(this);
                  
            broadCast("user_list_update/ ");          //9300
         } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Stream ���� �߻�", "�˸�", JOptionPane.ERROR_MESSAGE);  //9700
         }
      }

      @Override
      public void run() { // 4100
         System.out.println("start run");
         while (true) {
            try {
               String msg = dis.readUTF();
               System.out.println("read msg = "+ msg);
               textArea.append(userID + "�κ��� ������ �޽���: " + msg + "\n");
               recv_Msg(msg);                           // 6000

            } catch (IOException e) {                     // 7600
               // Ŭ���̾�Ʈ�� ������ ��������
               textArea.append(userID + " ����� ���� ������\n");
               try {
                  is.close();
                  os.close();
                  dos.close();
                  dis.close();
                  clientSocket.close();
                  user_vc.remove(this);
                  broadCast("User_out/" + userID);
                  broadCast("user_list_update/ ");      //9300

                  //room���� Ż��      //9810
                  for (int i = 0; i < room_vc.size(); i++) {
                     RoomInfo r = (RoomInfo) room_vc.elementAt(i);
                     if (r.Room_name.equals(roomID)) // �ش� ���� ã����
                     {
                        r.Room_user_vc.remove(this);    // ä�ù濡�� Ż��
                        if(r.Room_user_vc.size()==0) {
                           room_vc.remove(r);
                        }
                        break;
                     }
                  }   
               } catch (IOException e1) {
               }
               break;
            }
         }
      };
      
      public void recv_Msg(String str) {      // 6000
         st = new StringTokenizer(str, "/");
         String protocol = st.nextToken();
         String Message = st.nextToken();
         System.out.println("�������� : " + protocol);
         System.out.println("���� : " + Message);
         if(protocol.equals("Note")) {   
            // protocol = Note
            // message = user/~~~~~~
            String note = st.nextToken();

            System.out.println("�޴� ��� : " + Message); // user->Msg
            System.out.println("���� ���� : " + note);

            // ���Ϳ��� �ش� ����ڸ� ã�Ƽ� �޼��� ����
            for (int i = 0; i < user_vc.size(); i++) {
               UserInfo u = (UserInfo) user_vc.elementAt(i);
               if (u.userID.equals(Message)) {
                  u.send_Msg("NoteS/" + userID + "/" + note);    // userID�� �����Ѵٴ� ����
               }
            }
         } // Note if ��
         else if (protocol.equals("CreateRoom")) // 7000
         {
            // 1. ���� ���� ���� �ִ��� Ȯ�� �Ѵ�.      //9800
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(Message)) // ������ �̸��� ���� ������� ������
               {
                  send_Msg("CreateRoomFail/OK");
                  create_room_ok = false; // for������ ��� �˻��� ���� �������Ͽ�
                  break;
               }
            }
            if (create_room_ok) // ���� ���� �� ���� ��
            {

               RoomInfo r = new RoomInfo(Message, this);      //Message == RoomName, �����ÿ� room�� ����
               room_vc.add(r); // ���� �߰�
               roomID = Message;         //9810
               send_Msg("CreateRoom/" + Message);
               broadCast("New_Room/" + Message); // Ŭ���̾�Ʈ���� ���ο� �� ���� �뺸
            }
            create_room_ok = true; 
            
            
         } // CreateRoom
         else if (protocol.equals("Join_Room")) // 7300
         {
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(Message)) // �ش� ���� ã����
               {
                  r.BroadCast_Room("Join_Room_B/����/***" + userID + "���� �����ϼ̽��ϴ�.********");
                  r.Room_user_vc.add(this);    // ä�ù� ����, ����� �߰�
                  roomID = Message;         //9810
                  send_Msg("Join_Room/" + Message); // Message(=���̸�) �߰� 
                  break;
               }
            }
         }
         else if (protocol.equals("Chatting")) // 8000
         {
            // chatting / ä�ù��̸� / �޽��� ����
            String chatting_msg = st.nextToken();

            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(Message)) // �ش� ���� ã����
               {
                  r.BroadCast_Room("Chatting/" + userID + "/" + chatting_msg);
               }
            }
         } 
      }
      
      private void broadCast(String str) // ���� ����ڵ鿡�� ���ο� ����� �˸�
      {

         for (int i = 0; i < user_vc.size(); i++) {
            UserInfo u = (UserInfo) user_vc.elementAt(i);
            u.send_Msg(str);
         }

      }

      public void send_Msg(String msg) {
         try {
            dos.writeUTF(msg);
         } catch (IOException e) {         }
      }
   }
   
   class RoomInfo {                        //7000
      private String Room_name="";
      private Vector<UserInfo> Room_user_vc = new Vector<UserInfo>();

      RoomInfo(String name, UserInfo u) {   //�����ÿ� room�� ����
         this.Room_name = name;
         this.Room_user_vc.add(u);
      }
      
      public void BroadCast_Room(String str) {   //7300
         for (int i = 0; i < Room_user_vc.size(); i++) {
            UserInfo u = (UserInfo) Room_user_vc.elementAt(i);
            u.send_Msg(str);
         }
      }
   }
   
   @Override
   public void actionPerformed(ActionEvent e) { //1011      8600
      if (e.getSource() == start_btn) {
         System.out.println("Server Start button Click");
         Server_start(); // ���� ���� �� ����� ���
         
         start_btn.setEnabled(false);  //9100
         port_tf.setEditable(false);   //9100
         stop_btn.setEnabled(true);    //9100


      } else if (e.getSource() == stop_btn) {         //9000
         System.out.println("Server Stop Button Click");

         for (int i = 0; i < user_vc.size(); i++) {
            UserInfo u = (UserInfo) user_vc.elementAt(i);
            u.send_Msg("Server_Out/Bye");
            try {
               u.is.close();      //9800
               u.os.close();
               u.dos.close();
               u.dis.close();
               u.clientSocket.close();
               
            } catch (IOException e1) {

            }
         }
         try { 
            server_socket.close();
            stop_server = true;         //9800
            user_vc.removeAllElements();
            room_vc.removeAllElements();
         } catch (IOException e1) {

         }
         start_btn.setEnabled(true);  //9100
         port_tf.setEditable(true);   //9100
         stop_btn.setEnabled(false);    //9100
      }

   } // �׼� �̺�Ʈ ��

   public static void main(String[] args) { // 1000
      new ServerSend();
   }
}