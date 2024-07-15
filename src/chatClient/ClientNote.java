package chatClient;

import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ClientNote extends JFrame implements ActionListener, KeyListener {

   // Login GUI 변수
   private JFrame Login_GUI = new JFrame();
   private JPanel login_pane;
   private JTextField ip_tf; // IP 택스트 필드 Refactor-->Rename
   private JTextField port_tf; // port 택스트 필드
   private JTextField id_tf; // ID 택스트 필드
   private JButton login_btn = new JButton("접 속"); // 접속 버튼

   // Main GUI 변수
   private JPanel contentPane;
   private JTextField msg_tf;
   private JButton notesend_btn = new JButton("쪽지보내기");
   private JButton joinroom_btn = new JButton("채팅방참여");
   private JButton create_room_btn = new JButton("방만들기");
   private JButton send_btn = new JButton("전송");
   private JList<String> User_List = new JList(); // 전체 접속자 리스트
   private JList Room_List = new JList(); // 전체 방 목록 리스트
   private JTextArea Chat_area = new JTextArea(); // 채팅창 변수

   // network 변수 //2000
   Socket socket;
   String IP = "127.0.0.1";
   int port = 12345;
   String id; // 4100

   // Stream 변수 //3000
   InputStream is;
   DataInputStream dis;
   OutputStream os;
   DataOutputStream dos;

   // 클라이언트 관리 //5000
   Vector<String> user_list = new Vector<String>();
   Vector<String> roomListVC = new Vector<String>();
   StringTokenizer st;

   private String My_Room; // 내가 접속한 방 7100

   ClientNote() {
      login_init(); // 로그인 메뉴 화면
      main_init(); // 메인 메뉴 화면
      start(); // 리스너 연결

   }

   private void login_init() {
      Login_GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      Login_GUI.setBounds(300, 100, 295, 388);
      login_pane = new JPanel();
      login_pane.setBorder(new EmptyBorder(5, 5, 5, 5));
      Login_GUI.setContentPane(login_pane);
      login_pane.setLayout(null);

      JLabel lblNewLabel = new JLabel("Server IP");
      lblNewLabel.setBounds(12, 165, 57, 15);
      this.login_pane.add(lblNewLabel);

      JLabel lblNewLabel_1 = new JLabel("Sever Port");
      lblNewLabel_1.setBounds(12, 202, 69, 15);
      login_pane.add(lblNewLabel_1);

      JLabel lblNewLabel_2 = new JLabel("ID");
      lblNewLabel_2.setBounds(12, 245, 57, 15);
      login_pane.add(lblNewLabel_2);

      ip_tf = new JTextField();
      ip_tf.setBounds(92, 162, 116, 21);
      login_pane.add(ip_tf);
      ip_tf.setColumns(10);

      port_tf = new JTextField();
      port_tf.setBounds(92, 199, 116, 21);
      login_pane.add(port_tf);
      port_tf.setColumns(10);

      id_tf = new JTextField();
      id_tf.setBounds(92, 242, 116, 21);
      login_pane.add(id_tf);
      id_tf.setColumns(10);

      login_btn.setBounds(22, 291, 227, 23);
      login_pane.add(login_btn);
      Login_GUI.setVisible(true);
   }

   private void main_init() {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(600, 100, 510, 430);
      contentPane = new JPanel();
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);

      JLabel lblNewLabel = new JLabel("전체 접속자");
      lblNewLabel.setBounds(12, 20, 73, 15);
      contentPane.add(lblNewLabel);

      User_List.setBounds(12, 45, 108, 107);
      contentPane.add(User_List);

      notesend_btn.setBounds(12, 162, 108, 23);
      contentPane.add(notesend_btn);

      JLabel label = new JLabel("채팅방목록");
      label.setBounds(12, 195, 97, 15);
      contentPane.add(label);

      Room_List.setBounds(12, 210, 108, 107);
      contentPane.add(Room_List);

      joinroom_btn.setBounds(12, 327, 108, 23);
      contentPane.add(joinroom_btn);

      create_room_btn.setBounds(12, 356, 108, 23);
      contentPane.add(create_room_btn);

      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setBounds(142, 16, 340, 333);
      contentPane.add(scrollPane);

      scrollPane.setColumnHeaderView(Chat_area);

      msg_tf = new JTextField();
      msg_tf.setBounds(144, 357, 268, 21);
      contentPane.add(msg_tf);
      msg_tf.setColumns(10);

      send_btn.setBounds(412, 356, 70, 23);
      contentPane.add(send_btn);

      this.setVisible(true);
   }

   private void start() {
      login_btn.addActionListener(this); // 로그인 리스너 연결
      notesend_btn.addActionListener(this); // 쪽지 전송 리스너
      joinroom_btn.addActionListener(this); // 채팅방참여 리스너
      create_room_btn.addActionListener(this); // 방만들기 리스너
      send_btn.addActionListener(this); // 전송 버튼 리스너
      msg_tf.addKeyListener(this); // 전송버튼
   }

   private void network() { // 2100
      try {
         System.out.println(IP);
         socket = new Socket(IP, port);
         if (socket != null)
            connection();
         System.out.println("연결 생성 완료");
      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void connection() // 2100
   {
      try { // 3000
         is = socket.getInputStream();
         dis = new DataInputStream(is);
         os = socket.getOutputStream();
         dos = new DataOutputStream(os);

      } catch (IOException e) {
      }

      send_Msg(id); // 4100
      user_list.add(id); // 5000
      User_List.setListData(user_list); // 5000
      System.out.println(user_list);

      Thread th = new Thread(new Runnable() { // 4000
         public void run() {
            while (true) {
               try {
                  String msg = dis.readUTF();
                  System.out.println("서버로부터 수신: " + msg);
                  recv_Msg(msg);
               } catch (IOException e) {
               }
            }
         }
      });
      th.start();
   }

   private void send_Msg(String msg) { // 3000
      try {
         dos.writeUTF(msg);
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private void recv_Msg(String str) { // 3000 //5100
      st = new StringTokenizer(str, "/");
      String protocol = st.nextToken();
      String Message = st.nextToken();
      System.out.println("프로토콜 : " + protocol);
      System.out.println("내용 : " + Message);
      if (protocol.equals("NewUser")) {
         user_list.add(Message); // Message == clientID
         User_List.setListData(user_list);
         System.out.println(id+"   "+user_list);
      } else if (protocol.equals("OldUser")) {
         user_list.add(Message); // Message == clientID
         User_List.setListData(user_list);
         System.out.println(id+"   "+user_list);
      } else if (protocol.equals("NoteS")) { // 6100
         String note = st.nextToken();
         System.out.println(Message + " 사용자로부터 온 쪽지: " + note); // Message는 note를 전송하는 user
         JOptionPane.showMessageDialog(null, note, Message + "님으로부터 쪽지", JOptionPane.CLOSED_OPTION);
      } else if (protocol.equals("CreateRoom")) // 7100 서버가 CreateRoom, New_Room 동시에서 전송
      {
         My_Room = Message;
         Chat_area.append(id + "님이 " + My_Room + "을 생성 및 가입하였습니다.\n"); // 7500
      } else if (protocol.equals("New_Room")) // 7100
      {
         roomListVC.add(Message);
         Room_List.setListData(roomListVC);
         System.out.println(id+"   "+user_list);
      } else if (protocol.equals("Old_Room")) // 7200
      {
         roomListVC.add(Message);
         Room_List.setListData(roomListVC);
         System.out.println(id+"   "+Message);
      } else if (protocol.equals("Join_Room")) // 7500
      {
         My_Room = Message;      //Join 성공했으니 room 등록
         Chat_area.append(id + "님이 " + My_Room + "에 가입하였습니다.\n"); // id+메시지
      } else if (protocol.equals("Join_Room_B")) // 7500
      {
         String msg = st.nextToken();
         Chat_area.append(Message + " : " + msg + "\n"); // id+메시지
      } else if (protocol.equals("User_out")) // 7600
      {
         user_list.remove(Message);
         User_List.setListData(user_list);
         System.out.println(id+"   "+Message);
         // 탈퇴자 정보 display
         Chat_area.append(Message + "님 탈퇴 \n");
         // 같은 room에 가입한 경우 탈퇴 정보
         // 클라이언트간에는통신하지 않기에 스트림같은 것 없습니다.
      }
      else if (protocol.equals("Chatting")) // 8100
      {
         String msg = st.nextToken();
         Chat_area.append(Message + " : " + msg + "\n"); // id+메시지
      }
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == login_btn) {
         System.out.println("로그인 버튼 클릭");
//         IP = ip_tf.getText().trim();                   // 2200
//         port = Integer.parseInt(port_tf.getText().trim());    // 2200
         id = id_tf.getText().trim(); // 4100
         IP = "127.0.0.1";
         port = 12345;
         network(); // 2000
      } else if (e.getSource() == notesend_btn) { // 6000
         System.out.println("쪽지 보내기 버튼 클릭");
         String user = (String) User_List.getSelectedValue(); // 리스트에서 선택된 항목

         String note = JOptionPane.showInputDialog("보낼 메시지");
         if (note != null) {
            send_Msg("Note/" + user + "/" + note); // 예: Note/User2/Enjoy SOCEKET
         }
         System.out.println("받는 사람 : " + user + " | 보낼 데이터 : " + note);
      } else if (e.getSource() == joinroom_btn) { // 7400
         System.out.println("채팅방참여 버튼 클릭");
         String room = (String) Room_List.getSelectedValue(); // 리스트에서 선택된 항목

         if (room != null) {
            send_Msg("Join_Room/" + room);      //서버에게 room에 가입한다고...
         }
      } else if (e.getSource() == create_room_btn) { // 7000
         String room_name = JOptionPane.showInputDialog("방 이름");
         if (room_name != null) {
            send_Msg("CreateRoom/" + room_name);
         }
         System.out.println("방만들기 버튼 클릭");
      } else if (e.getSource() == send_btn) { // 8000

         System.out.println("전송 버튼 클릭");
         // send_Msg("전송 테스트 입니다"); // 4100
         if (!My_Room.isEmpty()) {
            send_Msg("Chatting/" + My_Room + "/" + msg_tf.getText().trim());
            msg_tf.setText("");
            msg_tf.requestFocus();
         }
      }
   }

   @Override
   public void keyPressed(KeyEvent e) {
      // TODO Auto-generated method stub

   }

   @Override
   public void keyReleased(KeyEvent e) {
      // TODO Auto-generated method stub

   }

   @Override
   public void keyTyped(KeyEvent e) {
      // TODO Auto-generated method stub

   }

   public static void main(String[] args) {
      new ClientNote();
   }

}