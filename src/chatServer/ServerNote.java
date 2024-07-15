package chatServer;

import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ServerNote extends JFrame implements ActionListener { // 0000, 0011
   // 자동 import 단축키 ctrl+shift+o 자동 채우기 ctrl+space
   private JPanel contentPane; // GUI Member 1011
   private JTextField port_tf;

   private JTextArea textArea = new JTextArea();
   private JButton start_btn = new JButton("서버 실행"); // Refactor-->Rename
   private JButton stop_btn = new JButton("서버 중지");

   // socket 생성 연결 부분 //2000
   private ServerSocket server_socket;
   private Socket socket;
   int port = 12345;

   // Stream 변수 //3000

   // 클라이언트 관리 //5000
   private Vector<UserInfo> user_vc = new Vector<UserInfo>();
   private Vector<RoomInfo> room_vc = new Vector<RoomInfo>();   //7000
   StringTokenizer st;      // 6000

   ServerNote() // 생성자 //1000
   {
      init(); // GUI 초기화, 화면 생성 //1000
      start(); // 리스너 설정 메소드 //1011
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

      JLabel lblNewLabel_2 = new JLabel("포트 번호");
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
      textArea.setEditable(false); // 6666 화면 에디트 설정

      stop_btn.setBounds(155, 286, 138, 23);
      contentPane.add(stop_btn);

      this.setVisible(true); // 화면 보이기
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
            while (true) { // 4000 여러명
               try {
                  textArea.append("사용자 접속 대기중\n");
                  socket = server_socket.accept();
                  textArea.append("클아이언트 접속 완료\n");
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

   } // 액션 이벤트 끝

   class UserInfo extends Thread { // 4000
      // Stream 변수
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
            textArea.append(userID + " 클라이언트 접속\n");
            
            // 연결되어 있는 클라이언트들에게 가입자 정보 전달
            System.out.println("현재 접속한 클라이언트 수 = "+ user_vc.size());
            for (int i = 0; i < user_vc.size(); i++) {         //5100
               UserInfo u = (UserInfo) user_vc.elementAt(i);
               u.send_Msg("NewUser/"+ userID);
               System.out.println("SEND NewUser");
            }
            
            //새로 접속한 클라이언트에게                        //5100
            for(int i=0; i<user_vc.size();i++) {
               UserInfo u = (UserInfo) user_vc.elementAt(i);
               send_Msg("OldUser/"+u.userID);
            }
            
            //새로 접속한 클라이언트 등록                        //5100
            user_vc.add(this);
            
            // 자신에게 기존의 개설된 채팅 방 정보 전송               //7200
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
               textArea.append(userID + "로부터 수신한 메시지: " + msg + "\n");
               recv_Msg(msg);                           // 6000

            } catch (IOException e) {                     // 7600
               // 클라이언트와 접속이 끊어지면
               textArea.append(userID + " 사용자 접속 끊어짐\n");
               try {
                  dos.close();
                  dis.close();
                  clientSocket.close();
                  user_vc.remove(this);      //등록 해제
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
         System.out.println("프로토콜 : " + protocol);
         System.out.println("내용 : " + Message);
         if(protocol.equals("Note")) {   
            // protocol = Note
            // message = user/~~~~~~
            String note = st.nextToken();

            System.out.println("받는 사람 : " + Message); // Message = 받는 클라이언트의 userID
            System.out.println("보낼 쪽지 : " + note);

            // 백터에서 해당 사용자를 찾아서 메세지 전송
            for (int i = 0; i < user_vc.size(); i++) {
               UserInfo u = (UserInfo) user_vc.elementAt(i);
               if (u.userID.equals(Message)) {
                  u.send_Msg("NoteS/" + userID + "/" + note);    // userID가 전송한다는 내용
               }
            }

         } // Note if 문
         else if (protocol.equals("CreateRoom")) // 7000
         {
               RoomInfo r = new RoomInfo(Message, this);   //Message == RoomName, 생성시에 room에 가입
               room_vc.add(r); // 방을 추가
               send_Msg("CreateRoom/" + Message); //CreateRoom을 전송한 클라이언트에게 전송
               broadCast("New_Room/" + Message); // 모든 클라이언트에게 새로운 방 생성했다고 통보
         } // CreateRoom
         else if (protocol.equals("Join_Room")) // 7300
         {
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(Message)) // 해당 방을 찾으면
               {
                  //방에 가입한 사람들에게 신고식...
                  r.BroadCast_Room("Join_Room_B/가입/***" + userID + "님이 입장하셨습니다.********");
                  r.Room_user_vc.add(this); // 채팅방 가입, 사용자 추가
                  send_Msg("Join_Room/" + Message); // Message(=방이름) 추가  너 가입 성공했어...
                  break;
               }
            }
         }
         else if (protocol.equals("Chatting")) // 8000
         {
            // chatting / 채팅방이름 / 메시지 전송
            String chatting_msg = st.nextToken();

            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(Message)) // 해당 방을 찾으면
               {
                  r.BroadCast_Room("Chatting/" + userID + "/" + chatting_msg);
               }
            }
         } 
      }
      
      private void broadCast(String str) // 기존 사용자들에게 새로운 사용자 알림
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

      RoomInfo(String name, UserInfo u) {   //생성시에 room에 가입
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