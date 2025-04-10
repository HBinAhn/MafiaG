package MafiaG;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import MafiaG.ConGemini;

public class Server {
    static List<ClientHandler> clients = new ArrayList<>();
    static final int MAX_CLIENTS = 7;
    static int anonymousCounter = 1;
    static int readyCount = 0;

    static Map<String, Integer> voteMap = new HashMap<>();
    static Set<String> votedUsers = new HashSet<>();
    static Map<String, String> answers = new HashMap<>();
    static int questionCount = 0;
    static final int MAX_QUESTIONS = 2;

    static List<String> questionList = Arrays.asList(
        "오늘 점심으로 뭘 먹을까요?",
        "당신이 제일 좋아하는 동물은?",
        "주말에 뭐하면 좋을까요?",
        "가장 기억에 남는 여행지는 어디인가요?",
        "요즘 즐겨 듣는 음악은 뭔가요?",
        "어릴 때 꿈은 무엇이었나요?",
        "요즘 빠진 취미는?",
        "혼자 여행 간다면 어디로 가고 싶나요?"
    );
    static List<String> usedQuestions = new ArrayList<>();
    static Random random = new Random();
    static String currentQuestion = "";

    static boolean resultRevealed = false;
    static boolean gameStarted = false;

    static ClientHandler geminiBot = null;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(3579)) {
            System.out.println("서버가 시작되었습니다");

            geminiBot = new GeminiBot("익명" + anonymousCounter++, getRandomColor());
            clients.add(geminiBot);

            while (true) {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(60000);
                if (clients.size() >= MAX_CLIENTS) {
                    socket.close();
                    continue;
                }

                ClientHandler handler = new ClientHandler(socket);
                handler.colorCode = getRandomColor();
                handler.nickname = "익명" + anonymousCounter++;
                clients.add(handler);

                handler.start();
                broadcastParticipants();
            }

        } catch (IOException e) {
        	System.err.println("서버 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void broadcast(String msg) {
        for (ClientHandler client : clients) {
            try {
                client.send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void broadcastParticipants() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"PARTICIPANTS\",\"list\":[");
        for (int i = 0; i < clients.size(); i++) {
            sb.append("{\"nickname\":\"").append(clients.get(i).nickname)
              .append("\",\"color\":\"").append(clients.get(i).colorCode).append("\"}");
            if (i != clients.size() - 1) sb.append(",");
        }
        sb.append("]}");
        broadcast(sb.toString());
    }

    
    static List<String> availableColors = new ArrayList<>(Arrays.asList(
    		"#FF6B6B", "#6BCB77", "#4D96FF", "#FFC75F", "#A66DD4", "#FF9671", "#00C9A7"));
    
    static String getRandomColor() {
    	if(availableColors.isEmpty()) {
    		return "#888888"; // fallback (혹은 오류 처리)
    	}
        return availableColors.remove(random.nextInt(availableColors.size()));
    }

    
    
    static void startNextQuestion() {
        if (questionCount >= MAX_QUESTIONS) {
            broadcast("{\"type\":\"GAME_OVER\",\"message\":\"질문이 모두 완료되었습니다.\"}");
            return;
        }
        
        do {
        	currentQuestion = questionList.get(random.nextInt(questionList.size()));
        } while (usedQuestions.contains(currentQuestion));

        usedQuestions.add(currentQuestion);
        questionCount++;
        resultRevealed = false;

        broadcast("{\"type\":\"QUESTION_PHASE\",\"question\":\"" + currentQuestion + "\"}");
        broadcast("{\"type\":\"chat\",\"color\":\"#888888\",\"message\":\"⏱️ 타이머 시작! 20초 후 답변이 공개됩니다.\"}");

        answers.clear();
        votedUsers.clear();
        voteMap.clear();

        // Gemini 자동 응답
        new Timer().schedule(new TimerTask() {
            public void run() {
                String geminiAnswer = generateGeminiAnswer(currentQuestion);
                answers.put(geminiBot.nickname, geminiAnswer);
                System.out.println("[서버] Gemini 답변 등록: " + geminiAnswer);
                checkAndRevealIfReady(); // Gemini 포함 즉시 공개 가능성 체크
            }
        }, 1000);

        // 20초 후 자동 공개
        new Timer().schedule(new TimerTask() {
            public void run() {
                if (!resultRevealed) {
                    revealAnswers();
                    resultRevealed = true;
                }
            }
        }, 20000);
    }

    static void revealAnswers() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"REVEAL_RESULT\",\"question\":\"")
          .append(currentQuestion).append("\",\"answers\":[");

        List<ClientHandler> shuffledClients = new ArrayList<>(clients);
        Collections.shuffle(shuffledClients);
        
        int i = 0;
        for (ClientHandler client : shuffledClients) {
            String answer = answers.get(client.nickname);
            if (answer == null) answer = "응답 없음";
            answer = answer.replace("\n", " ").replace("\"", "\\\""); 
            sb.append("{\"color\":\"").append(client.colorCode)
              .append("\",\"message\":\"").append(answer).append("\"}");
            if (++i < shuffledClients.size()) sb.append(",");
        }
        sb.append("]}");
        broadcast(sb.toString());

        new Timer().schedule(new TimerTask() {
            public void run() {
                broadcast("{\"type\":\"VOTE_PHASE\"}");
            }
        }, 1000);
    }


    static void checkAndRevealIfReady() {
    	if (answers.size() == clients.size() && !resultRevealed) {
            System.out.println("[서버] 모든 답변 제출됨 (하지만 20초 타이머까지 대기)");
        }
    	//// 답변이 모두 제출되었지만, 20초가 되기 전이면 기다리기만 함 (아무 것도 안 함)
        // → 타이머가 20초 후에 공개하도록 유도
    }
    
    static final Map<String, String> colorNameMap = new HashMap<String, String>() {{
        put("#FF6B6B", "빨강 유저");
        put("#6BCB77", "초록 유저");
        put("#4D96FF", "파랑 유저");
        put("#FFC75F", "노랑 유저");
        put("#A66DD4", "보라 유저");
        put("#FF9671", "오렌지 유저");
        put("#00C9A7", "청록 유저");
    }};

    static String getColorLabel(String colorOrNickname) {
        return colorNameMap.getOrDefault(colorOrNickname, colorOrNickname + " 유저"); // 기본값에 " 유저" 추가 유지
    }



    static void broadcastVoteResult() {
    	Map<String, Integer> voteCounts = new HashMap<>(voteMap);
        int maxVotes = 0;
        List<String> topColors = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : voteCounts.entrySet()) {
        	int votes =entry.getValue();
        	if ( votes > maxVotes) {
        		maxVotes = votes;
        		topColors.clear();
        		topColors.add(entry.getKey());
        	} else if (votes == maxVotes) {
                topColors.add(entry.getKey());
            }
        }
        
        for (Map.Entry<String, Integer> entry : voteMap.entrySet()) {
            totalVoteMap.put(entry.getKey(),
                totalVoteMap.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }

        
        List<String> namedWinners = new ArrayList<>();
        for (String color : topColors) {
            namedWinners.add(getColorLabel(color));
        }

        String winnerMsg = String.join(", ", namedWinners);
        broadcast("{\"type\":\"chat\",\"color\":\"#000000\",\"message\":\"💡 투표 결과: " + winnerMsg + " 유저가 "
            + maxVotes + "표를 받았습니다.\"}");

        new Timer().schedule(new TimerTask() {
            public void run() {
            	if (questionCount >= MAX_QUESTIONS) {
            	    broadcastFinalVoteResult();
            	} else {// ✅ 다음 라운드 시작
            	    startNextQuestion();
            	}
            }
        }, 3000);
    }
    static Map<String, Integer> totalVoteMap = new HashMap<>();

 // 최종 투표 결과 발표 메서드 수정 (동점자 전원 승리 처리)
    static void broadcastFinalVoteResult() {
        int maxVotes = 0;
        // 변수명을 topVotedColors 로 변경하여 색상 코드임을 명확히 함
        List<String> topVotedColors = new ArrayList<>();

        // totalVoteMap의 Key는 투표 대상(target), 즉 색상 코드
        for (Map.Entry<String, Integer> entry : totalVoteMap.entrySet()) {
            int votes = entry.getValue();
            String targetColor = entry.getKey(); // 색상 코드

            if (votes > maxVotes) {
                maxVotes = votes;
                topVotedColors.clear();
                topVotedColors.add(targetColor);
            } else if (votes == maxVotes && votes > 0) { // 0표 동점자는 제외
                topVotedColors.add(targetColor);
            }
        }

        // 승리자 이름 목록 생성 (색상 -> "색깔 유저" 변환)
        List<String> winnerLabels = new ArrayList<>();
        for (String color : topVotedColors) {
            winnerLabels.add(getColorLabel(color)); // getColorLabel 사용
        }

        String winnerMsg = String.join(", ", winnerLabels); // "빨강 유저, 파랑 유저" 형태

        // 최종 메시지 구성
        StringBuilder message = new StringBuilder("🏁 최종 투표 결과: ");
        if (winnerLabels.isEmpty()) {
            message.append("승자가 없습니다."); // 투표가 없거나 0표인 경우
        } else {
            // 변환된 레이블(winnerMsg)을 사용하여 메시지 생성
            message.append(winnerMsg)
                   .append(" (이)가 총 ").append(maxVotes).append("표를 받아 승리했습니다.");
        }

        // JSON escape 처리 추가 (메시지에 특수문자가 있을 경우 대비)
        String finalMessageJson = "{\"type\":\"FINAL_RESULT\",\"message\":\"" + escapeJson(message.toString()) + "\"}";
        broadcast(finalMessageJson);

        // 점수 업데이트 로직 호출 (파라미터는 topVotedColors 그대로 전달하거나,
        // 필요하다면 실제 닉네임 리스트로 변환하여 전달 - 현재 DatabaseManager 로직 확인 필요)
        // DatabaseManager.updateScoresAfterGame 가 색상 코드 리스트를 받는지, 닉네임 리스트를 받는지 확인 필요
        // 만약 닉네임 리스트가 필요하면, 여기서 색상->닉네임 변환 로직 추가 필요
        updateScores(topVotedColors); // 일단 색상 코드 리스트 전달
    }

    // 점수 반영 메서드 수정: 다수 승자 처리
 // 점수 반영 메서드 수정: 파라미터 타입을 명확히 (색상 코드 리스트)
    static void updateScores(List<String> winnerColors) { // 파라미터는 여전히 승리자의 색상 코드 리스트
        List<String> participantNicknames = new ArrayList<>(); // 참가자 닉네임 리스트 (Gemini 제외)
        Map<String, String> colorToNicknameMap = new HashMap<>(); // 색상-닉네임 매핑

        String geminiNickname = null; // Gemini 닉네임
        String geminiColor = null;    // Gemini 색상 코드

        // 현재 접속 중인 클라이언트 정보 수집
        for (ClientHandler client : clients) {
            // Gemini 정보 저장
            if (client instanceof GeminiBot) {
                geminiNickname = client.nickname;
                geminiColor = client.colorCode;
            }
            // 실제 플레이어 정보 저장
            else if (client.nickname != null) {
                participantNicknames.add(client.nickname); // 참가자 목록에 닉네임 추가
                if (client.colorCode != null) {
                    colorToNicknameMap.put(client.colorCode, client.nickname); // 색상-닉네임 매핑 저장
                }
            }
        }

        // 최종 승리자 닉네임 리스트 생성
        List<String> winnerNicknames = new ArrayList<>();
        for (String color : winnerColors) {
            if (color.equals(geminiColor)) {
                // 승자가 Gemini인 경우, Gemini 닉네임 추가
                if (geminiNickname != null) {
                    winnerNicknames.add(geminiNickname);
                }
            } else {
                // 승자가 플레이어인 경우, 색상 코드를 닉네임으로 변환하여 추가
                String nickname = colorToNicknameMap.get(color);
                if (nickname != null) {
                    winnerNicknames.add(nickname);
                } else {
                    System.err.println("[서버 경고] 승리자 색상(" + color + ")에 해당하는 닉네임을 찾을 수 없습니다.");
                }
            }
        }

        System.out.println("[서버] 점수 업데이트 호출 준비:");
        System.out.println("  승리자 닉네임 목록: " + winnerNicknames);
        System.out.println("  참가자 닉네임 목록: " + participantNicknames);

        // DatabaseManager.updateScoresAfterGame 호출 (닉네임 리스트 전달)
        try {
            DB.DatabaseManager.updateScoresAfterGame(winnerNicknames, participantNicknames);
            System.out.println("[서버] DatabaseManager.updateScoresAfterGame 호출 완료.");
        } catch (Exception e) {
            System.err.println("[서버 오류] 점수 반영 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    } // end of updateScores

    // JSON 문자열 내 특수문자 escape 처리 유틸리티 메소드 (필요시 추가)
    private static String escapeJson(String str) {
        if (str == null) return null;
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    

    static String generateGeminiAnswer(String question) {
        try {
            // Gemini에게 짧게 답변하라고 요청하는 프롬프트 추가
            String prompt = question + "\n\n위 질문에 대해 두 문장 이내로 간단하고 자연스럽게 대답해줘. 예를 들면 대화체처럼 말해줘.";
            String answer = ConGemini.getResponse(prompt);

            // 너무 긴 답변은 300자 이내로 자르기 (예외 방지용)
            if (answer.length() > 300) {
                answer = answer.substring(0, 300) + "...";
            }

            return answer;

        } catch (IOException e) {
            e.printStackTrace();
            return "Gemini 응답 실패: " + e.getMessage();
        }
    }



    static class ClientHandler extends Thread {
        Socket socket;
        BufferedReader br;
        BufferedWriter bw;
        String nickname;
        String colorCode;
        boolean isReady = false;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            if (socket != null) {
                try {
                    // ❗ 문자 인코딩 UTF-8 명시 ❗
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    System.err.println("[서버 오류] ClientHandler 스트림 생성 실패: " + e.getMessage());
                    e.printStackTrace();
                    // 생성자에서 오류 발생 시 자원 해제
                    try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ioex) {}
                }
            }
        }

        public void run() {
        	try {
                send("{\"type\":\"INIT\",\"nickname\":\"" + nickname + "\",\"color\":\"" + colorCode + "\"}");

                String msg;
                while ((msg = br.readLine()) != null) {
                    System.out.println("[서버 수신 " + nickname + "] " + msg); // <--- 어떤 메시지 받았는지 로그 추가

                    if (msg.contains("\"type\":\"start\"")) {
                        System.out.println("[서버 " + nickname + "] Start 메시지 감지됨!"); // <--- 감지 로그 추가
                        isReady = true;
                        readyCount++;
                        int realPlayers = clients.size() - 1;
                        //상태 로그
                        System.out.println("[서버] 준비 상태: " + readyCount + "/" + realPlayers);
                        
                        if (readyCount == realPlayers && realPlayers >= 1) {
                            System.out.println("[서버] 게임 시작 조건 충족! GAME_START 브로드캐스팅 시도..."); // <--- 브로드캐스트 시도 로그
                            broadcast("{\"type\":\"GAME_START\"}");
                            System.out.println("[서버] GAME_START 브로드캐스팅 완료."); // <--- 브로드캐스트 완료 로그
                            startNextQuestion();
                        } else {
                            System.out.println("[서버] 아직 게임 시작 조건 미충족."); // <--- 조건 미충족 로그
                        }
                    } else if (msg.contains("\"type\":\"ANSWER_SUBMIT\"")) {
                        String answer = extractValue(msg, "message");
                        System.out.println("[서버] " + nickname + " 의 답변 수신: " + answer);
                        answers.put(nickname, answer);
                        checkAndRevealIfReady();
                    } else if (msg.contains("\"type\":\"vote\"")) {
                        String targetColor = extractValue(msg, "target"); // 클라이언트가 보낸 대상의 색상 코드

                        voteMap.put(targetColor, voteMap.getOrDefault(targetColor, 0) + 1);
                        votedUsers.add(nickname);

                        // 모든 유저가 투표했을 때, 투표 결과를 발표
                        if (votedUsers.size() == clients.size() - 1) {
                            broadcastVoteResult();
                        }
                    } else if (!msg.trim().startsWith("{")) {
                        String chatJson = "{\"type\":\"chat\",\"color\":\"" + colorCode + "\",\"message\":\"" + msg + "\"}";
                        broadcast(chatJson);
                    }
                }
        	 } catch (Exception e) { // IOException 외 다른 예외도 잡기 위해 Exception 사용 (디버깅 목적)
                 System.err.println("[서버 오류 " + nickname + "] run 메소드 오류: " + e.getMessage());
                 e.printStackTrace();
            } finally {
                try {
                    clients.remove(this);

                    // ⭐️ 사용한 색상 되돌리기
                    if (colorCode != null && !availableColors.contains(colorCode)) {
                        availableColors.add(colorCode);
                    }

                    broadcastParticipants();
                    if (br != null) br.close();
                    if (bw != null) bw.close();
                    if (socket != null) socket.close();
                    
                } catch (IOException e) {
                	System.err.println("서버 소켓 종료 오류: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        void send(String msg) throws IOException {
            if (bw != null) {
                bw.write(msg);
                bw.newLine();
                bw.flush();
            }
        }
    }


    static class GeminiBot extends ClientHandler {
        public GeminiBot(String nickname, String colorCode) {
            super(null);
            this.nickname = nickname;
            this.colorCode = colorCode;
        }

        @Override public void run() {}
        @Override void send(String msg) {}
    }

    static String extractValue(String json, String key) {
        int idx = json.indexOf(key);
        if (idx == -1) return "";
        int start = json.indexOf("\"", idx + key.length() + 2);
        int end = json.indexOf("\"", start + 1);
        return json.substring(start + 1, end);
    }
    
 // 기존 코드 아래에 추가
    static void handleTryAgain() {
        // 게임 상태 초기화
        resetGameState();

        // 새로운 게임 시작 메시지 브로드캐스트
        broadcast("{\"type\":\"GAME_START\"}");

        // 새로운 질문을 시작
        startNextQuestion();
        
        // 타이머를 새로 설정
        startNewRoundTimer();
    }

    // 게임 상태 초기화 메서드
    static void resetGameState() {
        questionCount = 0;  // 질문 카운트 초기화
        usedQuestions.clear();  // 사용된 질문 목록 초기화
        voteMap.clear();  // 투표 맵 초기화
        votedUsers.clear();  // 투표한 유저 초기화
        answers.clear();  // 답변 초기화
        resultRevealed = false;  // 결과 공개 상태 초기화
        readyCount = 0;  // 준비된 유저 수 초기화
    }

    // 새로운 라운드를 위한 타이머 설정
    static void startNewRoundTimer() {
        new Timer().schedule(new TimerTask() {
            public void run() {
                // 20초 타이머 후, 자동으로 답변 공개
                if (!resultRevealed) {
                    revealAnswers();
                    resultRevealed = true;
                }
            }
        }, 20000);
    }

    
}
