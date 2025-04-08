package DB;

import java.sql.*;

public class DatabaseManager {
	/*
	 * private static final String URL = "jdbc:mysql://localhost:3306/mafia_game";
	 * private static final String USER = ""; // 본인 DB 유저명 private static final
	 * String PASSWORD = ""; // 본인 DB 비밀번호
	 */    
	
	private static final String URL = "jdbc:mysql://localhost:3306/mafiag";
	private static final String USER = "root"; // 본인 DB 유저명 private static final
	public static String PASSWORD = "1234";
	
    // 로그인
	public static boolean checkLogin(String id, String password) {
        String sql = "SELECT * FROM member WHERE member_id = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // 결과가 있으면 로그인 성공
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
	
	// 아이디 찾기
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
	
	// 비밀번호 찾기
	public static boolean findPasswordByEmailAndId(String id, String email) {
	    String sql = "SELECT * FROM member WHERE member_id = ? AND email = ?";
	    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, id);
	        pstmt.setString(2, email);
	        ResultSet rs = pstmt.executeQuery();
	        return rs.next(); // 결과가 있으면 정보 일치
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	// ID 중복 확인
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

    // 닉네임 중복 확인
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

    // 이메일 중복 확인
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

    // 회원 가입 처리
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

    // 유저의 현재 점수 가져오기
    public static int getUserScore(String username) {
        int score = 0;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 누적점수 FROM users WHERE 회원아이디 = ?")) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                score = rs.getInt("누적점수");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return score;
    }

    // 유저의 점수 업데이트 (게임 후 반영)
    public static void updateUserScore(String username, int scoreToAdd) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE users SET 누적점수 = 누적점수 + ? WHERE 회원아이디 = ?")) {

            pstmt.setInt(1, scoreToAdd);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 로그아웃 (종료 전 처리)
    public static void logoutUser(String username) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE users SET last_login = NOW() WHERE 회원아이디 = ?")) {

            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
