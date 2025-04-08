package MafiaG;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    static List<ClientHandler> clients = new ArrayList<>();
    static final int MAX_CLIENTS = 7;
    static int anonymousCounter = 1;
    static int readyCount = 0;

    static Map<String, Integer> voteMap = new HashMap<>();
    static Set<String> votedUsers = new HashSet<>();
    static Map<String, String> answers = new HashMap<>();
    static int questionCount = 0;
    static final int MAX_QUESTIONS = 5;

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
    static Random random = new Random();
    static String currentQuestion = "";

    static boolean resultRevealed = false;

    static ClientHandler geminiBot = null;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(3579)) {
            System.out.println("서버가 시작되었습니다");

            geminiBot = new GeminiBot("익명" + anonymousCounter++, getRandomColor());
            clients.add(geminiBot);

            while (true) {
                Socket socket = serverSocket.accept();
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

    static String getRandomColor() {
        String[] colors = {"#FF6B6B", "#6BCB77", "#4D96FF", "#FFC75F", "#A66DD4", "#FF9671", "#00C9A7"};
        return colors[random.nextInt(colors.length)];
    }

    static void startNextQuestion() {
        if (questionCount >= MAX_QUESTIONS) {
            broadcast("{\"type\":\"GAME_OVER\",\"message\":\"질문이 모두 완료되었습니다.\"}");
            return;
        }

        currentQuestion = questionList.get(random.nextInt(questionList.size()));
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

        int i = 0;
        for (ClientHandler client : clients) {
            String answer = answers.get(client.nickname);
            if (answer == null) answer = "응답 없음";
            sb.append("{\"color\":\"").append(client.colorCode)
              .append("\",\"message\":\"").append(answer).append("\"}");
            if (++i < clients.size()) sb.append(",");
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
//        if (answers.size() == clients.size() && !resultRevealed) {
//            resultRevealed = true;
//            revealAnswers();
        }
    	//// 답변이 모두 제출되었지만, 20초가 되기 전이면 기다리기만 함 (아무 것도 안 함)
        // → 타이머가 20초 후에 공개하도록 유도
    }

    static void broadcastVoteResult() {
        String topColor = null;
        int maxVotes = 0;

        for (Map.Entry<String, Integer> entry : voteMap.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                topColor = entry.getKey();
            }
        }

        broadcast("{\"type\":\"chat\",\"color\":\"#000000\",\"message\":\"💡 투표 결과: " + topColor + " 유저가 " + maxVotes + "표를 받았습니다.\"}");

        new Timer().schedule(new TimerTask() {
            public void run() {
                startNextQuestion(); // ✅ 다음 라운드 시작
            }
        }, 3000);
    }

    static String generateGeminiAnswer(String question) {
        String[] sample = {"비빔밥이요!", "고양이요!", "넷플릭스요!", "스페인이요!", "잔잔한 재즈요!"};
        return sample[random.nextInt(sample.length)];
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
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void run() {
            try {
                send("{\"type\":\"INIT\",\"nickname\":\"" + nickname + "\",\"color\":\"" + colorCode + "\"}");

                String msg;
                while ((msg = br.readLine()) != null) {
                    if (msg.contains("\"type\":\"start\"")) {
                        isReady = true;
                        readyCount++;
                        int realPlayers = clients.size() - 1;
                        if (readyCount == realPlayers && realPlayers >= 1) {
                            broadcast("{\"type\":\"GAME_START\"}");
                            startNextQuestion();
                        }
                    } else if (msg.contains("\"type\":\"ANSWER_SUBMIT\"")) {
                        String answer = extractValue(msg, "message");
                        System.out.println("[서버] " + nickname + " 의 답변 수신: " + answer);
                        answers.put(nickname, answer);
                        checkAndRevealIfReady();
                    } else if (msg.contains("\"type\":\"vote\"")) {
                        String target = extractValue(msg, "target");
                        voteMap.put(target, voteMap.getOrDefault(target, 0) + 1);
                        votedUsers.add(nickname);
                        if (votedUsers.size() == clients.size()-1) {
                            broadcastVoteResult();
                        }
                    } else if (!msg.trim().startsWith("{")) {
                        String chatJson = "{\"type\":\"chat\",\"color\":\"" + colorCode + "\",\"message\":\"" + msg + "\"}";
                        broadcast(chatJson);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clients.remove(this);
                    broadcastParticipants();
                    if (br != null) br.close();
                    if (bw != null) bw.close();
                    if (socket != null) socket.close();
                } catch (IOException e) {
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
}
