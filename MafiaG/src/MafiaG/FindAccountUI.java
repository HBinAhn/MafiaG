package MafiaG;

import javax.swing.*;
import java.awt.*;
import DB.DatabaseManager;

public class FindAccountUI extends JFrame {
    public FindAccountUI(Runnable backToLogin) {
        setTitle("���̵�/��й�ȣ ã��");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 248, 248));

        JPanel centerBox = new JPanel(new GridBagLayout());
        centerBox.setOpaque(false);

        JPanel innerBox = new JPanel();
        innerBox.setOpaque(false);
        innerBox.setLayout(new BoxLayout(innerBox, BoxLayout.Y_AXIS));
        innerBox.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));

        // �ΰ�
        JPanel logoZone = new JPanel();
        logoZone.setOpaque(false);
        JLabel logoLabel = new JLabel();
        ImageIcon logoIcon = new ImageIcon("../../MafiaG_logo.jpg");
        Image rawImage = logoIcon.getImage();
        double aspectRatio = (double) rawImage.getWidth(null) / rawImage.getHeight(null);
        int width = 200, height = (int)(200 / aspectRatio);
        if (height > 100) {
            height = 100;
            width = (int)(100 * aspectRatio);
        }
        logoLabel.setIcon(new ImageIcon(rawImage.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        logoZone.add(logoLabel);

        // �޽��� ��¿� ��
        JLabel messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("���� ���", Font.BOLD, 15));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ���̵� ã�� �ڽ�
        JPanel idBox = createFindBox("���̵� ã��", new String[]{"�̸���"}, (inputs) -> {
            String email = inputs[0].getText().trim();
            if (email.isEmpty()) {
                setMessage(messageLabel, "�̸����� �Է����ּ���.", false);
                return;
            }
            String foundId = DatabaseManager.findMemberIdByEmail(email);
            if (foundId != null) {
                String maskedId = foundId.substring(0, 3) + repeatChar('*', Math.max(0, foundId.length() - 3));
                setMessage(messageLabel, "�Է��Ͻ� �̸��Ϸ� ������ ���̵�� " + maskedId + " �Դϴ�.", true);
            } else {
                setMessage(messageLabel, "�Է��Ͻ� �̸��Ϸ� ������ ���̵� ã�� �� �����ϴ�.", false);
            }
        });

        // ��й�ȣ ã�� �ڽ�
        JPanel pwBox = createFindBox("��й�ȣ ã��", new String[]{"���̵�", "�̸���"}, (inputs) -> {
            String id = inputs[0].getText().trim();
            String email = inputs[1].getText().trim();
            if (id.isEmpty() || email.isEmpty()) {
                setMessage(messageLabel, "���̵�� �̸����� ��� �Է����ּ���.", false);
                return;
            }
            boolean match = DatabaseManager.findPasswordByEmailAndId(id, email);
            if (match) {
                setMessage(messageLabel, "��й�ȣ �缳�� ��ũ�� �̸��Ϸ� �߼��߽��ϴ�.", true);
            } else {
                setMessage(messageLabel, "�Է��Ͻ� ������ ������ ������ ã�� �� �����ϴ�.", false);
            }
        });

        JPanel findZone = new JPanel(new GridLayout(1, 2, 40, 0));
        findZone.setOpaque(false);
        findZone.setMaximumSize(new Dimension(700, 300));
        findZone.add(idBox);
        findZone.add(pwBox);

        // ���ư��� ��ư
        JButton backButton = new JButton("���� �������� ���ư���");
        backButton.setMaximumSize(new Dimension(500, 90));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setBackground(new Color(204, 230, 255));
        backButton.setForeground(new Color(68, 68, 68));
        backButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        backButton.addActionListener(e -> {
            dispose();
            if (backToLogin != null) backToLogin.run();
        });

        // ����
        innerBox.add(logoZone);
        innerBox.add(Box.createVerticalStrut(10));
        innerBox.add(findZone);
        innerBox.add(Box.createVerticalStrut(30));
        innerBox.add(messageLabel);
        innerBox.add(Box.createVerticalStrut(30));
        innerBox.add(backButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        centerBox.add(innerBox, gbc);
        mainPanel.add(centerBox, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createFindBox(String title, String[] labels, java.util.function.Consumer<JTextField[]> onSubmit) {
        JPanel box = new JPanel();
        box.setBackground(Color.WHITE);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(titleLabel);
        box.add(Box.createVerticalStrut(15));

        JTextField[] inputs = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            JPanel inputGroup = new JPanel();
            inputGroup.setLayout(new BoxLayout(inputGroup, BoxLayout.X_AXIS));
            inputGroup.setOpaque(false);

            JLabel lbl = new JLabel(labels[i]);
            lbl.setPreferredSize(new Dimension(80, 40));
            lbl.setFont(new Font("SansSerif", Font.BOLD, 14));

            JTextField input = new JTextField();
            input.setPreferredSize(new Dimension(200, 40));
            input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            input.setFont(new Font("SansSerif", Font.PLAIN, 16));
            input.setBackground(new Color(227, 232, 236));

            inputGroup.add(lbl);
            inputGroup.add(Box.createHorizontalStrut(10));
            inputGroup.add(input);
            box.add(inputGroup);
            box.add(Box.createVerticalStrut(10));
            inputs[i] = input;
        }

        JButton button = new JButton(title);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(204, 230, 255));
        button.setForeground(new Color(68, 68, 68));
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.addActionListener(e -> onSubmit.accept(inputs));
        box.add(button);

        return box;
    }

    private void setMessage(JLabel label, String text, boolean success) {
        label.setText(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(success ? new Color(119, 206, 105) : new Color(255, 91, 91));
    }

    // Java 8 ȣȯ�� repeat �Լ�
    private String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
