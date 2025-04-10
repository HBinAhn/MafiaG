package MafiaG;

import javax.swing.*;

import DB.DatabaseManager;

import java.awt.*;
import java.awt.event.*;

public class MafiaGResult extends JFrame {
    private String username;
    private boolean isWinner;
    private int scoreEarned;

    public MafiaGResult(String username, boolean isWinner) {
        this.username = username;
        this.isWinner = isWinner;
        this.scoreEarned = isWinner ? 5 : -1;

        DatabaseManager.updateUserScore(username, scoreEarned);
        int updatedScore = DatabaseManager.getUserScore(username);

        setTitle("MafiaG");
        ImageIcon logoIcon = new ImageIcon("src/MafiaG_logo.png");
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

        // 결과 텍스트
        JLabel resultLabel = new JLabel(isWinner ? "승리" : "패배", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 50));
        resultLabel.setForeground(new Color(50, 130, 200));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT); 
        resultLabel.setBorder(BorderFactory.createEmptyBorder(60, 0, 30, 0)); // 여백 조정

        // 이미지
        String imagePath = isWinner ? "src/victory.png" : "src/defeat.png";
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
        ImageIcon quitIcon = new ImageIcon("src/quit_button.png");
        ImageIcon playIcon = new ImageIcon("src/playagain_button.png");

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
        againButton.addActionListener(e -> {
            dispose();
            new PlayUI();
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
