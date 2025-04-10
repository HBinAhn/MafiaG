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
        JButton againButton = new JButton();

        // 아이콘
        ImageIcon quitIcon = new ImageIcon("src/img/quit_button.png");
        ImageIcon playIcon = new ImageIcon("src/img/playagain_button.png");

        Image resizedQuit = quitIcon.getImage().getScaledInstance(150, 110, Image.SCALE_SMOOTH);
        Image resizedPlay = playIcon.getImage().getScaledInstance(200, 100, Image.SCALE_SMOOTH);

        quitButton.setIcon(new ImageIcon(resizedQuit));
        againButton.setIcon(new ImageIcon(resizedPlay));

        quitButton.setPreferredSize(new Dimension(150, 110));
        againButton.setPreferredSize(new Dimension(200, 100));

        quitButton.setBorderPainted(false);
        quitButton.setContentAreaFilled(false);
        quitButton.setFocusPainted(false);

        againButton.setBorderPainted(false);
        againButton.setContentAreaFilled(false);
        againButton.setFocusPainted(false);

        quitButton.addActionListener(e -> logoutAndExit());
        // "try again" 버튼 클릭 시 게임을 다시 시작하는 로직 추가
        againButton.addActionListener(e -> {
            dispose();  // 현재 게임 결과 화면을 닫고
            new PlayUI();  // 새 게임 화면을 여는 코드
        });

        buttonPanel.add(quitButton, BorderLayout.WEST);
        buttonPanel.add(againButton, BorderLayout.EAST);

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
            return "참여자 승리!";
        } else if (isPlayerLoser) {
            return "참여자 패배...";
        } else {
            return "게임 종료";
        }
    }

    // 게임 결과에 따른 이미지 반환
    private String getImagePath(boolean isGeminiWinner, boolean isPlayerWinner, boolean isPlayerLoser) {
        if (isGeminiWinner) {
            return "src/img/victory.png";  // Gemini 승리 이미지
        } else if (isPlayerWinner) {
            return "src/img/victory.png";  // 참여자 승리 이미지
        } else if (isPlayerLoser) {
            return "src/img/defeat.png";  // 참여자 패배 이미지
        } else {
            return "src/img/defeat.png";  // 기본 이미지
        }
    }

    private void logoutAndExit() {
        DatabaseManager.logoutUser(username);
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