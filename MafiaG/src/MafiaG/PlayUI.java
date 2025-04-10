package MafiaG;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;


public class PlayUI extends JFrame implements ActionListener {
	static Socket sock;
	static BufferedWriter bw = null;
	static BufferedReader br = null;

	private DefaultListModel<String> participantModel;
//	private JTextArea rankingArea;
	private RankingPanel rankingPanel;
	private JTextField chatInput;
	private JTextPane chatPane;
	private StyledDocument doc;
	private JButton startButton;
	private JComboBox<String> voteChoice;
	private JButton voteBtn;
	private JLabel timerLabel;
	
	// 튜토리얼 관련
	private JPanel chatContainerCards; // CardLayout이 적용된 패널
	private CardLayout cardLayout;

	private String myColor = "";
	private boolean gameStarted = false;
	private String myNickname = "";


	private final Map<String, String> colorToNameMap = new HashMap<String, String>() {{
	    put("#FF6B6B", "빨강 유저");
	    put("#6BCB77", "초록 유저");
	    put("#4D96FF", "파랑 유저");
	    put("#FFC75F", "노랑 유저");
	    put("#A66DD4", "보라 유저");
	    put("#FF9671", "오렌지 유저");
	    put("#00C9A7", "청록 유저");
	}};

	private final Map<String, String> nameToColorMap = new HashMap<>();
	

	public PlayUI() {
		setTitle("MafiaG");
		setSize(1200, 800);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout());
		
	    // 로고 이미지를 포함한 헤더 생성
	    JPanel header = new JPanel(new BorderLayout());
	    header.setBackground(new Color(238, 238, 238));
	    header.setBorder(new EmptyBorder(10, 20, 10, 20));
	    
	    // 로고 이미지 추가
	    ImageIcon icon = new ImageIcon("src/img/logo.png"); // 로고 경로
	    JLabel logoLabel = new JLabel(icon);
	    header.add(logoLabel, BorderLayout.WEST); // 로고를 왼쪽에 배치

	    // 앱 이름 추가
	    JLabel titleLabel = new JLabel("MafiaG", SwingConstants.LEFT);
	    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));  // 타이틀 글꼴 설정
	    header.add(titleLabel, BorderLayout.CENTER); // 타이틀을 중앙에 배치

	    // 기존 코드 그대로 header를 JFrame에 추가
	    add(header, BorderLayout.NORTH);
		
		setupUI();
		connectToServer();
		setLocationRelativeTo(null);

		 addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowClosing(WindowEvent e) {
	                int result = JOptionPane.showConfirmDialog(
	                    PlayUI.this,
	                    "정말 종료하시겠습니까?",
	                    "종료 확인",
	                    JOptionPane.YES_NO_OPTION
	                );
	                
	                System.out.println("종료 요청 확인됨");
	                
	                if (result == JOptionPane.YES_OPTION) {
	                	System.out.println("정리 작업 시작");
	                	
	                 // 네트워크 자원 정리
	                    closeConnection();
	                    System.out.println("연결 종료");
	                    sendToServer("{\"type\":\"quit\"}");
	                    try {
	                        Thread.sleep(1000); // 자원 해제 대기
	                    } catch (InterruptedException e1) {
	                        e1.printStackTrace();
	                    }
	                    
	                 // 창 종료
	                    dispose();
	                    
	                 // 모든 스레드 정리 후 강제 종료
	                    System.exit(0);
	                    System.out.println("완전 종료");
	                    
	                }
	            }
	        });
	        setLocationRelativeTo(null);
	    }

	private void setupUI() {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(new Color(238, 238, 238));
		header.setBorder(new EmptyBorder(10, 20, 10, 20));
		header.add(new JLabel("MafiaG", SwingConstants.LEFT), BorderLayout.WEST);
		add(header, BorderLayout.NORTH);

		JPanel mainPanel = new JPanel(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		

		JPanel sidebar = new JPanel(new BorderLayout());
		sidebar.setPreferredSize(new Dimension(200, 0));
		sidebar.setBackground(new Color(240, 234, 255));

		// 랭킹창
		JPanel sidebarContent = new JPanel(new BorderLayout());  // 선언 먼저
//		rankingArea = new JTextArea("랭킹\n", 5, 20);
//		rankingArea.setEditable(false);
//		JScrollPane rankingScroll = new JScrollPane(rankingArea);
//		rankingScroll.setBorder(BorderFactory.createTitledBorder("랭킹"));
		rankingPanel = new RankingPanel();
		sidebarContent.add(rankingPanel, BorderLayout.NORTH);

		// 참여자 명단
		participantModel = new DefaultListModel<>();
		JList<String> participantList = new JList<>(participantModel);
		JScrollPane participantScroll = new JScrollPane(participantList);
		participantScroll.setBorder(BorderFactory.createTitledBorder("참여자 명단"));

		startButton = new JButton("Start");
		startButton.setEnabled(true);
		startButton.setPreferredSize(new Dimension(200, 50));
		startButton.addActionListener(e -> {
			startButton.setEnabled(false);
			sendToServer("{\"type\":\"start\"}");
		});

//		JPanel sidebarContent = new JPanel(new BorderLayout());
//		sidebarContent.add(rankingScroll, BorderLayout.NORTH);
		sidebarContent.add(participantScroll, BorderLayout.CENTER);
		sidebar.add(sidebarContent, BorderLayout.CENTER);
		sidebar.add(startButton, BorderLayout.SOUTH);
		mainPanel.add(sidebar, BorderLayout.WEST);

		// 채팅창
		// CardLayout을 위한 컨테이너
		cardLayout = new CardLayout();
		chatContainerCards = new JPanel(cardLayout);

		// 튜토리얼 이미지
		ImageIcon tutorialImage = new ImageIcon("src/img/TutorialSample.png"); // 파일 경로 맞춰서 수정
		JLabel tutorialLabel = new JLabel(tutorialImage);
		tutorialLabel.setHorizontalAlignment(JLabel.CENTER);
		JPanel tutorialPanel = new JPanel(new BorderLayout());
		tutorialPanel.add(tutorialLabel, BorderLayout.CENTER);

		
		JPanel chatContainer = new JPanel(new BorderLayout());
		chatPane = new JTextPane();
		chatPane.setEditable(false);
		doc = chatPane.getStyledDocument();
		JScrollPane chatScroll = new JScrollPane(chatPane);
		chatContainer.add(chatScroll, BorderLayout.CENTER);
		
		// 기존 채팅창 패널을 카드에 추가
		chatContainerCards.add(tutorialPanel, "tutorial");
		chatContainerCards.add(chatContainer, "chat");

		// 채팅 입력창 input text
		JPanel inputPanel = new JPanel(new BorderLayout());
		chatInput = new JTextField();
		chatInput.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
		chatInput.setEnabled(false);
		chatInput.setBackground(Color.LIGHT_GRAY);
		chatInput.addActionListener(this);
		inputPanel.add(chatInput, BorderLayout.CENTER);
		chatContainer.add(inputPanel, BorderLayout.SOUTH);
//		mainPanel.add(chatContainer, BorderLayout.CENTER); // 튜토리얼 이미지 반영 전
		mainPanel.add(chatContainerCards, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		voteChoice = new JComboBox<>();
		voteChoice.setPreferredSize(new Dimension(150, 30));
		voteChoice.setEnabled(false);

		voteBtn = new JButton("투표");
		voteBtn.setEnabled(false);
		voteBtn.addActionListener(e -> {
			String selectedLabel = (String) voteChoice.getSelectedItem();
			if (selectedLabel != null) {
				String selectedColor = nameToColorMap.get(selectedLabel);
				if (selectedColor != null) {
					sendToServer("{\"type\":\"vote\",\"target\":\"" + selectedColor + "\"}");
					voteChoice.setEnabled(false);
					voteBtn.setEnabled(false);
				}
			}
		});

		timerLabel = new JLabel("남은 시간: 20초");
		bottomPanel.add(new JLabel("투표 대상:"));
		bottomPanel.add(voteChoice);
		bottomPanel.add(voteBtn);
		bottomPanel.add(timerLabel);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent e) {
		String msg = chatInput.getText().trim();
		if (!msg.isEmpty()) {
			sendToServer("{\"type\":\"ANSWER_SUBMIT\",\"message\":\"" + msg + "\"}");
			appendAnonymousChat(myColor, msg); // ✅ 내 답변도 미리 출력
			chatInput.setText("");
		}
	}


	private void appendAnonymousChat(String colorCode, String msg) {
		SimpleAttributeSet attr = new SimpleAttributeSet();
		try {
			StyleConstants.setForeground(attr, Color.decode(colorCode));
		} catch (NumberFormatException e) {
			StyleConstants.setForeground(attr, Color.GRAY);
		}
		StyleConstants.setFontSize(attr, 16);
		try {
			doc.insertString(doc.getLength(), msg + "\n", attr);
			doc.setParagraphAttributes(doc.getLength(), 1, attr, false);
			chatPane.setCaretPosition(doc.getLength());
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
	}

	 private void connectToServer() {
			try {
	            // ❗ 서버 IP 주소 확인!
	            sock = new Socket("172.30.1.47", 3579);
				br = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
				bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8));


				// 서버 메시지 수신 스레드
				 Thread serverThread = new Thread(() -> {
		                String line;
		                try {
		                    while ((line = br.readLine()) != null) {
		                        String finalLine = line;
		                        System.out.println("서버로부터: " + finalLine);

							// === 메시지 타입별 처리 ===

							// 1. 초기화 (INIT) 메시지: 내 정보(색상, 닉네임) 설정
							if (finalLine.contains("\"type\":\"INIT\"")) {
								myColor = extractValue(finalLine, "color");
	                            myNickname = extractValue(finalLine, "nickname");
	                            System.out.println("[클라이언트] 내 정보 설정: Nick=" + myNickname + ", Color=" + myColor);
							}
	                        // 2. 참가자 목록 (PARTICIPANTS) 메시지: UI 갱신 (투표 목록, 참여자 목록)
	                        else if (finalLine.contains("\"type\":\"PARTICIPANTS\"")) {
								// Swing UI 업데이트는 Event Dispatch Thread(EDT)에서 처리해야 함
								SwingUtilities.invokeLater(() -> {
	                                System.out.println("[클라이언트] 참가자 목록 수신. 내 색상: " + myColor); // 로그 추가
									// 기존 목록 초기화
	                                voteChoice.removeAllItems(); // 투표 드롭다운 초기화
									nameToColorMap.clear();     // 이름-색상 맵 초기화
									participantModel.clear();  // 참여자 JList 모델 초기화

	                                // JSON 파싱 (안정성을 위해 라이브러리 사용 권장 - 예: Gson, Jackson)
	                                // 현재는 문자열 기반 파싱 유지
	                                try {
	                                    // "list":[...] 부분 추출
	                                    int listStartIndex = finalLine.indexOf("\"list\":[") + "\"list\":[".length();
	                                    int listEndIndex = finalLine.lastIndexOf("]");
	                                    // 리스트 데이터가 비어있지 않은 경우에만 처리
	                                    if (listEndIndex > listStartIndex) {
	                                        String listData = finalLine.substring(listStartIndex, listEndIndex);
	                                        // 각 참가자 정보 객체 분리 ("{...},{...}")
	                                        String[] entries = listData.split("\\}(?=\\s*,\\s*\\{)"); // 정규표현식 수정: }, { 사이 공백 허용

	                                        for (String entry : entries) {
	                                            // 각 entry가 완전한 JSON 객체 형태가 되도록 보정
	                                            String currentEntry = entry.trim();
	                                            if (!currentEntry.startsWith("{")) {
	                                                currentEntry = "{" + currentEntry;
	                                            }
	                                            // 마지막 항목의 누락된 } 추가 (split 로직 개선 필요 시 제거 가능)
	                                            if (!currentEntry.endsWith("}")) {
	                                                currentEntry = currentEntry + "}";
	                                            }


	                                            String nickname = extractValue(currentEntry, "nickname");
	                                            String color = extractValue(currentEntry, "color");

	                                            System.out.println("  처리 중인 참가자: Nick=" + nickname + ", Color=" + color); // 로그

	                                            // 유효한 데이터인 경우에만 처리
	                                            if (color != null && !color.isEmpty() && nickname != null) {
	                                                // 표시될 레이블 생성 ("색깔 유저" 또는 닉네임)
	                                                String label = colorToNameMap.getOrDefault(color, nickname + " (" + color.substring(1) + ")"); // colorToNameMap 활용

	                                                // 참여자 명단(JList)에는 자기 자신 포함 모든 참여자 추가
	                                                participantModel.addElement(label);

	                                                // ⭐ 자기 자신(myColor)은 투표 드롭다운(voteChoice)에서 제외 ⭐
	                                                if (!color.equals(myColor)) {
	                                                     System.out.println("    투표 목록에 추가: " + label); // 로그
	                                                     voteChoice.addItem(label); // 드롭다운에 추가
	                                                     nameToColorMap.put(label, color); // 레이블 -> 색상 매핑 저장
	                                                 } else {
	                                                     System.out.println("    투표 목록에서 제외 (본인): " + label); // 로그
	                                                 }
	                                            } else {
	                                                 System.out.println("    잘못된 참가자 데이터 건너뜀: " + currentEntry); // 로그
	                                            }
	                                        }
	                                    } else {
	                                        System.out.println("[클라이언트] 참가자 목록 데이터가 비어있습니다.");
	                                    }
	                                } catch (Exception e) {
	                                    System.err.println("[클라이언트] PARTICIPANTS 메시지 파싱 오류: " + finalLine);
	                                    e.printStackTrace();
	                                }
								});
							}
	                        // 3. 질문 단계 (QUESTION_PHASE) 메시지: 질문 표시, 타이머 시작
	                        else if (finalLine.contains("\"type\":\"QUESTION_PHASE\"")) {
								String question = extractValue(finalLine, "question");
								SwingUtilities.invokeLater(() -> {
									appendAnonymousChat("#444444", "❓ 질문: " + question); // 질문 채팅창에 표시
									chatInput.setEnabled(true); // 답변 입력 활성화
	                                chatInput.setBackground(Color.WHITE);
	                                chatInput.requestFocus(); // 입력창 포커스

									// 타이머 시작 (20초 카운트다운)
									new Thread(() -> {
	                                    timerLabel.setVisible(true); // 타이머 보이게 설정
										for (int i = 20; i >= 0; i--) {
											int sec = i;
											// UI 업데이트는 EDT에서
											SwingUtilities.invokeLater(() -> timerLabel.setText("남은 시간: " + sec + "초"));
											try {
												Thread.sleep(1000); // 1초 대기
											} catch (InterruptedException ex) {
	                                             System.out.println("[클라이언트] 타이머 스레드 중단됨.");
												Thread.currentThread().interrupt(); // 인터럽트 상태 복원
												break; // 타이머 중단
											}
										}
	                                    // 타이머 종료 후
	                                    SwingUtilities.invokeLater(() -> {
	                                        timerLabel.setText("시간 종료");
	                                        chatInput.setEnabled(false); // 시간 종료 시 입력 비활성화
	                                        chatInput.setBackground(Color.LIGHT_GRAY);
	                                    });
									}).start();
								});
							}
	                        // 4. 일반 채팅 (chat) 메시지: 채팅창에 표시
	                        else if (finalLine.contains("\"type\":\"chat\"")) {
								String color = extractValue(finalLine, "color");
								String msg = extractValue(finalLine, "message");
	                            // 채팅 메시지는 항상 EDT에서 처리
								SwingUtilities.invokeLater(() -> appendAnonymousChat(color, msg));
							}
	                        // 5. 답변 공개 (REVEAL_RESULT) 메시지: 답변 목록 표시
	                        else if (finalLine.contains("\"type\":\"REVEAL_RESULT\"")) {
	                            // 답변 공개 메시지 채팅창에 표시 (항상 EDT)
							    SwingUtilities.invokeLater(() -> appendAnonymousChat("#444444", "💬 모든 답변이 공개되었습니다!"));

	                            // 답변 목록 파싱 및 표시
							    try {
	                                // "answers":[...] 부분 추출
							        int answersStartIndex = finalLine.indexOf("\"answers\":[") + "\"answers\":[".length();
							        int answersEndIndex = finalLine.lastIndexOf("]}"); // ]} 까지 포함해야 함
							        if (answersEndIndex > answersStartIndex) {
							            String answersData = finalLine.substring(answersStartIndex, answersEndIndex);
	                                    // 각 답변 객체 분리 ("{...},{...}")
	                                    String[] items = answersData.split("\\}(?=\\s*,\\s*\\{)");

							            for (String item : items) {
	                                        String currentItem = item.trim();
	                                        if (!currentItem.startsWith("{")) {
	                                            currentItem = "{" + currentItem;
	                                        }
	                                        if (!currentItem.endsWith("}")) {
	                                            currentItem = currentItem + "}";
	                                        }

							                String color = extractValue(currentItem, "color");
							                String message = extractValue(currentItem, "message");
	                                        // 답변 메시지 표시 (항상 EDT)
	                                        SwingUtilities.invokeLater(() -> appendAnonymousChat(color, "💬 " + message));
							            }
							        }
							    } catch (Exception ex) {
	                                System.err.println("[클라이언트] REVEAL_RESULT 메시지 파싱 오류: " + finalLine);
							        ex.printStackTrace();
							    }
							}
	                        // 6. 투표 단계 (VOTE_PHASE) 메시지: 투표 UI 활성화
	                        else if (finalLine.contains("\"type\":\"VOTE_PHASE\"")) {
	                            // 투표 관련 UI 활성화 (항상 EDT)
								SwingUtilities.invokeLater(() -> {
	                                appendAnonymousChat("#0000FF", "🗳️ 이제 투표할 시간입니다! 드롭다운에서 의심되는 유저를 선택하고 투표 버튼을 누르세요.");
									voteChoice.setEnabled(true); // 드롭다운 활성화
									voteBtn.setEnabled(true);    // 투표 버튼 활성화
	                                timerLabel.setVisible(false); // 이전 라운드 타이머 숨기기
								});
							}
	                        // 7. 게임 시작 (GAME_START) 메시지: 게임 UI 활성화, 튜토리얼->채팅 전환
	                        else if (finalLine.contains("\"type\":\"GAME_START\"")) {
	                            System.out.println("[클라이언트] GAME_START 메시지 감지됨!"); // <--- 감지 로그
	                            SwingUtilities.invokeLater(() -> {
	                                try { // invokeLater 내부에서도 예외 처리
	                                    System.out.println("[클라이언트 EDT] 게임 시작 UI 업데이트 시작..."); // <--- EDT 시작 로그
	                                    gameStarted = true;
	                                    // chatInput.setEnabled(true); // 질문 나올 때 활성화
	                                    // chatInput.setBackground(Color.WHITE);
	                                    startButton.setEnabled(false);
	                                    startButton.setText("게임 진행 중...");
	                                    System.out.println("[클라이언트 EDT] CardLayout 전환 시도..."); // <--- 전환 시도 로그
	                                    cardLayout.show(chatContainerCards, "chat");
	                                    System.out.println("[클라이언트 EDT] CardLayout 전환 완료."); // <--- 전환 완료 로그
	                                    appendAnonymousChat("#008000", "🎉 게임이 시작되었습니다! 첫 번째 질문을 기다려주세요...");
	                                } catch (Exception e) {
	                                    System.err.println("[클라이언트 EDT 오류] GAME_START UI 업데이트 중 오류:");
	                                    e.printStackTrace();
	                                }
	                            });
	                        }
	                        // 8. 최종 결과 (FINAL_RESULT) 메시지: 결과 표시, 게임 종료 처리
	                        else if (finalLine.contains("\"type\":\"FINAL_RESULT\"")) {
							    String msg = extractValue(finalLine, "message");
	                            // 최종 결과 메시지 표시 및 UI 비활성화 (항상 EDT)
							    SwingUtilities.invokeLater(() -> {
	                                System.out.println("[클라이언트] 최종 결과 수신!");
							        appendAnonymousChat("#FF0000", msg); // 강조 색상으로 결과 표시

							        // 게임 종료 시점에 관련 UI 비활성화
							        chatInput.setEnabled(false);
							        chatInput.setBackground(Color.LIGHT_GRAY);
							        voteChoice.setEnabled(false);
							        voteBtn.setEnabled(false);
							        startButton.setEnabled(false); // 시작 버튼 비활성화 유지
	                                startButton.setText("게임 종료");
							        timerLabel.setText("게임 종료");
	                                timerLabel.setVisible(true);

	                                // ⭐ 여기에 승/패 화면 전환 로직 추가 필요 ⭐
	                                // 예: showResultScreen(msg);
	                                // JOptionPane.showMessageDialog(PlayUI.this, msg, "게임 종료", JOptionPane.INFORMATION_MESSAGE);
	                                // 이후 PlayUI 창을 닫고 MafiaGResult 창을 여는 로직 구현
								});
							}
	                        // 9. 게임 오버 (GAME_OVER) 메시지 (질문 모두 소진 등)
	                         else if (finalLine.contains("\"type\":\"GAME_OVER\"")) {
	                             String msg = extractValue(finalLine, "message");
	                             SwingUtilities.invokeLater(() -> {
	                                 appendAnonymousChat("#FF8C00", msg); // 주황색 등으로 표시
	                                 // 필요시 UI 비활성화 추가
	                                  chatInput.setEnabled(false);
	                                  chatInput.setBackground(Color.LIGHT_GRAY);
	                                  voteChoice.setEnabled(false);
	                                  voteBtn.setEnabled(false);
	                                  startButton.setEnabled(false);
	                                  timerLabel.setText("게임 종료됨");
	                                  timerLabel.setVisible(true);
	                             });
	                         }

						}
					} catch (IOException e) {
	                    // 서버 연결 끊김 또는 소켓 닫힘
	                     if (sock != null && sock.isClosed()) {
	                         System.out.println("[클라이언트] 소켓 연결이 정상적으로 종료되었습니다.");
	                     } else {
	                         System.err.println("[클라이언트] 서버 연결이 끊어졌습니다: " + e.getMessage());
	                         // 사용자에게 알림 (EDT에서 실행)
	                         SwingUtilities.invokeLater(()-> {
	                             JOptionPane.showMessageDialog(PlayUI.this,
	                                 "서버와의 연결이 끊어졌습니다. 프로그램을 종료합니다.",
	                                 "연결 오류", JOptionPane.ERROR_MESSAGE);
	                             // 연결 종료 및 창 닫기
	                             closeConnection();
	                             dispose();
	                             System.exit(1); // 오류 종료
	                         });
	                     }
					} finally {
	                     // 스레드 종료 시 자원 정리 (이미 closeConnection 호출된 경우 중복될 수 있으나 안전하게)
	                     // closeConnection(); // 필요시 여기서도 호출
	                     System.out.println("[클라이언트] 서버 리스너 스레드 종료.");
	                 }
				});
				serverThread.setDaemon(true); // 메인 스레드 종료 시 같이 종료되도록 설정
				serverThread.start(); // 스레드 시작

			} catch (IOException e) {
				e.printStackTrace();
	            // 초기 서버 연결 실패 시 사용자 알림
				JOptionPane.showMessageDialog(this, "서버 연결에 실패했습니다: " + e.getMessage() + "\n프로그램을 종료합니다.", "연결 실패", JOptionPane.ERROR_MESSAGE);
	            System.exit(1); // 프로그램 종료
			}
		} // end of connectToServer

	private void sendToServer(String message) {
		try {
			if (bw != null) {
				bw.write(message + "\n");
				bw.flush();
			}
		} catch (IOException ex) {
			System.out.println("서버로 메시지 전송 실패");
			closeConnection();
		}
	}

	private void closeConnection() {
		try {
     	    if (sock != null && !sock.isClosed()) {
     	        sock.shutdownInput();  // 👈 먼저 입력 스트림 닫기
     	        sock.shutdownOutput(); // 👈 출력도 명시적으로 종료
     	    }
     	} catch (IOException e) {
     	    e.printStackTrace();
     	    System.err.println("sock 닫기 실패: " + e.getMessage());
     	}
     	
     	    try {
     	        if (br != null) {
     	            br.close();
     	        }
     	    } catch (IOException e) {
     	        System.err.println("br 닫기 실패: " + e.getMessage());
     	    }
 
     	    try {
     	        if (bw != null) {
     	            bw.close();
     	        }
     	    } catch (IOException e) {
     	        System.err.println("bw 닫기 실패: " + e.getMessage());
     	    }
	}

	private String extractValue(String json, String key) {
		try {
			String pattern = "\"" + key + "\":\"";
			int start = json.indexOf(pattern) + pattern.length();
			int end = json.indexOf("\"", start);
			return json.substring(start, end);
		} catch (Exception e) {
			return "";
		}
	}
}