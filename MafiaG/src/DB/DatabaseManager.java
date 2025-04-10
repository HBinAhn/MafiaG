package DB;

import java.sql.*;

public class DatabaseManager {
	/*
	 * private static final String URL = "jdbc:mysql://localhost:3306/mafia_game";
	 * private static final String USER = ""; // ���� DB ������ private static final
	 * String PASSWORD = ""; // ���� DB ��й�ȣ
	 */    
	
	private static final String URL = "jdbc:mysql://localhost:3306/mafiag";
	private static final String USER = "root"; // ���� DB ������ private static final
	public static String PASSWORD = "0000";
	
    // �α���
	public static boolean checkLogin(String id, String password) {
        String sql = "SELECT * FROM member WHERE member_id = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // ����� ������ �α��� ����
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
	
	// ���̵� ã��
	public static String findMemberIdByEmail(String email) {
        String sql = "SELECT member_id FROM member WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("member_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	// ��й�ȣ ã��
	public static boolean findPasswordByEmailAndId(String id, String email) {
	    String sql = "SELECT * FROM member WHERE member_id = ? AND email = ?";
	    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, id);
	        pstmt.setString(2, email);
	        ResultSet rs = pstmt.executeQuery();
	        return rs.next(); // ����� ������ ���� ��ġ
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	// ID �ߺ� Ȯ��
    public static boolean isIdDuplicate(String id) {
        String sql = "SELECT COUNT(*) FROM Member WHERE member_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // �г��� �ߺ� Ȯ��
    public static boolean isNicknameDuplicate(String nickname) {
        String sql = "SELECT COUNT(*) FROM Member WHERE nickname = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // �̸��� �ߺ� Ȯ��
    public static boolean isEmailDuplicate(String email) {
        String sql = "SELECT COUNT(*) FROM Member WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ȸ�� ���� ó��
    public static boolean insertNewMember(String id, String password, String nickname, String email) {
        String sql = "INSERT INTO Member (member_id, password, email, nickname) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, nickname);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ������ ���� ���� ��������
    public static int getUserScore(String username) {
        int score = 0;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT �������� FROM users WHERE ȸ�����̵� = ?")) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                score = rs.getInt("��������");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return score;
    }

    // ������ ���� ������Ʈ (���� �� �ݿ�)
    public static void updateUserScore(String username, int scoreToAdd) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE users SET �������� = �������� + ? WHERE ȸ�����̵� = ?")) {

            pstmt.setInt(1, scoreToAdd);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // �α׾ƿ� (���� �� ó��)
    public static void logoutUser(String username) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE users SET last_login = NOW() WHERE ȸ�����̵� = ?")) {

            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
