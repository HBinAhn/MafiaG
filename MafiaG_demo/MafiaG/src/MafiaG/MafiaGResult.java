package MafiaG;

import javax.swing.*;
import DB.DatabaseManager;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MafiaGResult extends JFrame {
	private String username;
	private List<String> winners; // 승리자 목록
	private List<String> participants; // 참가자 목록

	public MafiaGResult(String username, List<String> winners, List<String> participants) {
		this.username = username;
		this.winners = winners;
		this.participants = participants;

		// 점수 처리 로직
		boolean isGeminiWinner = winners.contains("Gemini");
		boolean isPlayerWinner = winners.contains(username) && !isGeminiWinner;
		boolean isPlayerLoser = !winners.contains(username) && !isGeminiWinner;

		// 점수 갱신 (Gemini 또는 참여자 승리/패배에 따른 점수 갱신)
//        updateUserScore(isGeminiWinner, isPlayerWinner, isPlayerLoser);

		setTitle("MafiaG");
		ImageIcon logoIcon = new ImageIcon("src/img/logo.png");
		setIconImage(logoIcon.getImage());
		setSize(1200, 800);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setResizable(false);

		GradientPanel contentPane = new GradientPanel();
		contentPane.setLayout(new BorderLayout(10, 10));
		setContentPane(contentPane);

		// 중앙 패널: 텍스트 + 이미지
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setOpaque(false); // 배경 투명

		// 결과 텍스트 설정
		String resultText = getResultText(isGeminiWinner, isPlayerWinner, isPlayerLoser);
		JLabel resultLabel = new JLabel(resultText, SwingConstants.CENTER);
		resultLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 55));
		resultLabel.setForeground(new Color(50, 130, 200));
		resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		resultLabel.setBorder(BorderFactory.createEmptyBorder(60, 0, 30, 0)); // 여백 조정

		// 이미지 설정
		String imagePath = getImagePath(isGeminiWinner, isPlayerWinner, isPlayerLoser);
		ImageIcon icon = new ImageIcon(imagePath);
		Image img = icon.getImage().getScaledInstance(640, 384, Image.SCALE_SMOOTH);
		JLabel imageLabel = new JLabel(new ImageIcon(img));
		imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		centerPanel.add(resultLabel);
		centerPanel.add(imageLabel);
		contentPane.add(centerPanel, BorderLayout.CENTER);

		// 버튼 패널
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setOpaque(false);

		JButton quitButton = new JButton();

		// 아이콘
		ImageIcon quitIcon = new ImageIcon("src/img/quit_button.png");
		ImageIcon playIcon = new ImageIcon("src/img/playagain_button.png");

		Image resizedQuit = quitIcon.getImage().getScaledInstance(150, 110, Image.SCALE_SMOOTH);
		Image resizedPlay = playIcon.getImage().getScaledInstance(200, 100, Image.SCALE_SMOOTH);

		quitButton.setIcon(new ImageIcon(resizedQuit));

		quitButton.setPreferredSize(new Dimension(150, 110));

		quitButton.setBorderPainted(false);
		quitButton.setContentAreaFilled(false);
		quitButton.setFocusPainted(false);


		quitButton.addActionListener(e -> logoutAndExit()); {
		// "try again" 버튼 클릭 시 게임을 다시 시작하는 로직 추가
			dispose(); // 현재 게임 결과 화면을 닫고

			// --- ❗ PlayUI 생성자에 사용자 이름(실제 닉네임) 전달 ❗ ---
			if (this.username != null && !this.username.isEmpty()) {
				System.out.println("[MafiaGResult] 새 게임 시작. 사용자: " + this.username);
				new PlayUI(this.username); // <- 생성자에 this.username 전달
			} else {
				// username이 없는 예외적인 경우 처리 (예: 로그인 화면으로 이동)
				System.err.println("[MafiaGResult 오류] 사용자 이름(username)이 없어 새 게임을 시작할 수 없습니다.");
				JOptionPane.showMessageDialog(this, "오류: 사용자 정보를 찾을 수 없습니다. 로그인 화면으로 돌아갑니다.", "오류",
						JOptionPane.ERROR_MESSAGE);
				// new LoginUI().showLoginUI(); // 예시: 로그인 화면으로 이동
				System.exit(1); // 또는 프로그램 종료
			}
			// --- 수정 끝 ---
		};

		buttonPanel.add(quitButton, BorderLayout.EAST);

		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		// 창 닫기 이벤트
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				logoutAndExit();
			}
		});

		setVisible(true);
	}

	// 게임 결과에 따른 텍스트 반환
	private String getResultText(boolean isGeminiWinner, boolean isPlayerWinner, boolean isPlayerLoser) {
		if (isGeminiWinner) {
			return "Gemini 승리!";
		} else if (isPlayerWinner) {
			return "당신은 AI를 지배하는 자";
		} else if (isPlayerLoser) {
			return "Gemini가 되지 못하였습니다.";
		} else {
			return "게임 종료";
		}
	}

	// 게임 결과에 따른 이미지 반환
	private String getImagePath(boolean isGeminiWinner, boolean isPlayerWinner, boolean isPlayerLoser) {
		if (isGeminiWinner) {
			return "src/img/defeat.png"; // Gemini 승리 이미지
		} else if (isPlayerWinner) {
			return "src/img/victory.png"; // 참여자 승리 이미지
		} else if (isPlayerLoser) {
			return "src/img/defeat.png"; // 참여자 패배 이미지
		} else {
			return "src/img/defeat.png"; // 기본 이미지
		}
	}

	private void logoutAndExit() {
//		DatabaseManager.logoutUser(username);
		JOptionPane.showMessageDialog(null, "로그아웃 되었습니다!");
		System.exit(0);
	}

	// 내부 클래스: 그라데이션 배경 패널
	class GradientPanel extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			Color color1 = new Color(180, 210, 255);
			Color color2 = new Color(255, 200, 200);
			int width = getWidth();
			int height = getHeight();
			GradientPaint gp = new GradientPaint(0, 0, color1, width, height, color2);
			g2d.setPaint(gp);
			g2d.fillRect(0, 0, width, height);
		}
	}
}