package chatServer;

import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ServerNote extends JFrame implements ActionListener { // 0000, 0011
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

   // Ŭ���̾�Ʈ ���� //5000
   private Vector<UserInfo> user_vc = new Vector<UserInfo>();
   private Vector<RoomInfo> room_vc = new Vector<RoomInfo>();   //7000
   StringTokenizer st;      // 6000

   ServerNote() // ������ //1000
   {
      init(); // GUI �ʱ�ȭ, ȭ�� ���� //1000
      start(); // ������ ���� �޼ҵ� //1011
      Server_start(); // 2000

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
//         port = Integer.parseInt(port_tf.getText().trim());
         server_socket = new ServerSocket(port); // 2200
      } catch (IOException e) {
         e.printStackTrace();
      }
      if (server_socket != null)
         connection(); // 1000
   }

   public void connection() { // 2000

      Thread th = new Thread(new Runnable() { // 2300
         public void run() {
            while (true) { // 4000 ������
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

   @Override
   public void actionPerformed(ActionEvent e) { // 1011
      if (e.getSource() == start_btn) {
         System.out.println("Server Start button Click");
      } else if (e.getSource() == stop_btn) {
         System.out.println("Server Stop Button Click");
      }

   } // �׼� �̺�Ʈ ��

   class UserInfo extends Thread { // 4000
      // Stream ����
      private InputStream is;
      private DataInputStream dis;
      private OutputStream os;
      private DataOutputStream dos;
      private Socket clientSocket;
      private String userID = "";

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
            
            //���� ������ Ŭ���̾�Ʈ ���                        //5100
            user_vc.add(this);
            
            // �ڽſ��� ������ ������ ä�� �� ���� ����               //7200
            for (int i = 0; i < room_vc.size(); i++) 
            {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               send_Msg("Old_Room/" + r.Room_name);
            }
            
         } catch (IOException e) {
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
                  dos.close();
                  dis.close();
                  clientSocket.close();
                  user_vc.remove(this);      //��� ����
                  broadCast("User_out/" + userID);
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

            System.out.println("�޴� ��� : " + Message); // Message = �޴� Ŭ���̾�Ʈ�� userID
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
               RoomInfo r = new RoomInfo(Message, this);   //Message == RoomName, �����ÿ� room�� ����
               room_vc.add(r); // ���� �߰�
               send_Msg("CreateRoom/" + Message); //CreateRoom�� ������ Ŭ���̾�Ʈ���� ����
               broadCast("New_Room/" + Message); // ��� Ŭ���̾�Ʈ���� ���ο� �� �����ߴٰ� �뺸
         } // CreateRoom
         else if (protocol.equals("Join_Room")) // 7300
         {
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(Message)) // �ش� ���� ã����
               {
                  //�濡 ������ ����鿡�� �Ű��...
                  r.BroadCast_Room("Join_Room_B/����/***" + userID + "���� �����ϼ̽��ϴ�.********");
                  r.Room_user_vc.add(this); // ä�ù� ����, ����� �߰�
                  send_Msg("Join_Room/" + Message); // Message(=���̸�) �߰�  �� ���� �����߾�...
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

   public static void main(String[] args) { // 1000
      new ServerNote();
   }
}