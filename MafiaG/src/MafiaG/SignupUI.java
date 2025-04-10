package MafiaG;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SignupUI extends JFrame {

    public SignupUI(Runnable onSignupComplete) {
        setTitle("ȸ������ ������");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel();
        contentPane.setBackground(new Color(248, 248, 248));
        contentPane.setLayout(new GridBagLayout());
        setContentPane(contentPane);

        JPanel centerBox = new JPanel();
        centerBox.setPreferredSize(new Dimension(560, 600));
        centerBox.setOpaque(false);
        centerBox.setLayout(new BorderLayout());

        JPanel logoPanel = new JPanel();
        logoPanel.setPreferredSize(new Dimension(560, 180));
        logoPanel.setOpaque(false);
        JLabel logoLabel = new JLabel();
        ImageIcon logoIcon = new ImageIcon("../../MafiaG_logo.jpg");
        logoLabel.setIcon(new ImageIcon(logoIcon.getImage().getScaledInstance(230, 160, Image.SCALE_SMOOTH)));
        logoPanel.add(logoLabel);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 1, 0, 10));
        formPanel.setOpaque(false);

        formPanel.add(createInputGroup("���̵�", JTextField.class));
        formPanel.add(createInputGroup("��й�ȣ", JPasswordField.class));
        formPanel.add(createInputGroup("��й�ȣ Ȯ��", JPasswordField.class));
        formPanel.add(createInputGroup("�г���", JTextField.class));
        formPanel.add(createInputGroup("�̸���", JTextField.class));

        JButton signupButton = new JButton("ȸ������ �Ϸ�");
        signupButton.setPreferredSize(new Dimension(0, 45));
        signupButton.setBackground(new Color(204, 230, 255));
        signupButton.setForeground(new Color(68, 68, 68));
        signupButton.setFont(new Font("���� ���", Font.BOLD, 16));
        signupButton.setFocusPainted(false);
        signupButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        signupButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        signupButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                signupButton.setBackground(new Color(179, 218, 255));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                signupButton.setBackground(new Color(204, 230, 255));
            }
        });

        signupButton.addActionListener(e -> {
            // �Է°� ��������
            String id = ((JTextField)((JPanel) formPanel.getComponent(0)).getComponent(1)).getText();
            String pw = new String(((JPasswordField)((JPanel) formPanel.getComponent(1)).getComponent(1)).getPassword());
            String pwConfirm = new String(((JPasswordField)((JPanel) formPanel.getComponent(2)).getComponent(1)).getPassword());
            String nickname = ((JTextField)((JPanel) formPanel.getComponent(3)).getComponent(1)).getText();
            String email = ((JTextField)((JPanel) formPanel.getComponent(4)).getComponent(1)).getText();

            // ��й�ȣ Ȯ��
            if (!pw.equals(pwConfirm)) {
                JOptionPane.showMessageDialog(SignupUI.this, "��й�ȣ�� ��ġ���� �ʽ��ϴ�.");
                return;
            }

            // DB�� ȸ������ ����
            boolean success = DB.DatabaseManager.insertNewMember(id, pw, nickname, email);

            if (success) {
                JOptionPane.showMessageDialog(SignupUI.this, "ȸ�������� �Ϸ�Ǿ����ϴ�.");
                dispose(); // â �ݱ�
                onSignupComplete.run(); // �α��� ȭ������ �̵�
            } else {
                JOptionPane.showMessageDialog(SignupUI.this, "ȸ�����Կ� �����߽��ϴ�. �ٽ� �õ����ּ���.");
            }
        });	
        
        
        JPanel formContainer = new JPanel();
        formContainer.setOpaque(false);
        formContainer.setLayout(new BorderLayout(0, 20));
        formContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        formContainer.add(formPanel, BorderLayout.CENTER);
        formContainer.add(signupButton, BorderLayout.SOUTH);

        centerBox.add(logoPanel, BorderLayout.NORTH);
        centerBox.add(formContainer, BorderLayout.CENTER);
        contentPane.add(centerBox);

        setVisible(true);
    }

    private JPanel createInputGroup(String labelText, Class<? extends JComponent> inputType) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(120, 40));
        label.setFont(new Font("���� ���", Font.BOLD, 14));
        label.setForeground(new Color(51, 51, 51));
        label.setHorizontalAlignment(SwingConstants.LEFT);

        JComponent input;
        if (inputType == JPasswordField.class) {
            input = new JPasswordField();
        } else {
            input = new JTextField();
        }
        input.setPreferredSize(new Dimension(240, 40));
        input.setFont(new Font("���� ���", Font.PLAIN, 16));
        input.setBackground(new Color(227, 232, 236));
        input.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        panel.add(label, BorderLayout.WEST);
        panel.add(input, BorderLayout.CENTER);

        return panel;
    }
}
