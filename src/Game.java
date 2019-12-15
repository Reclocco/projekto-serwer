import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Game {

    private Player[][] board;
    private int[][] logicBoard;
    private Player currentPlayer;
    private int boardSize;
    private List<String> connections1 = new ArrayList<>();
    private List<String> connections2 = new ArrayList<>();


    private synchronized void move(int row, int col , Player player) {
        if (player != currentPlayer) {
            throw new IllegalStateException("Not your turn");
        } else if (player.opponent == null) {
            throw new IllegalStateException("You don't have an opponent yet");
        } else if (board[row][col] != null) {
            throw new IllegalStateException("Cross already occupied");
        } else if (!moveLogicCheck(row, col)) {
            throw new IllegalStateException("Dont kill yourself");
        }
        board[row][col] = currentPlayer;
        currentPlayer = currentPlayer.opponent;
    }


    private boolean moveLogicCheck(int row, int col) {

        boolean oneDies;
        boolean twoDies;

        connections1.clear();
        connections2.clear();

        //filling logic board
        logicBoard = new int[boardSize+2][boardSize+2];
        for(int a=0;a<boardSize+2;a++){
            for(int b=0;b<boardSize+2;b++){
                if(a==0||a==boardSize+1||b==0||b==boardSize+1) logicBoard[a][b]=-1;
                else if(board[a-1][b-1]==currentPlayer) logicBoard[a][b]=1;
                else if(board[a-1][b-1]==currentPlayer.opponent) logicBoard[a][b]=2;
                else logicBoard[a][b]=0;
            }
        }
        logicBoard[row+1][col+1] = 1;

        //making connection arraylist
        for(int a=1;a<boardSize+1;a++){
            for(int b=1;b<boardSize+1;b++){
                if(logicBoard[a][b] != 0){
                    if(logicBoard[a][b] == logicBoard[a-1][b]) createConnection(a,b,a-1,b,logicBoard[a][b]);
                    if(logicBoard[a][b] == logicBoard[a+1][b]) createConnection(a,b,a+1,b,logicBoard[a][b]);
                    if(logicBoard[a][b] == logicBoard[a][b-1]) createConnection(a,b,a,b-1,logicBoard[a][b]);
                    if(logicBoard[a][b] == logicBoard[a][b+1]) createConnection(a,b,a,b+1,logicBoard[a][b]);
                    if(logicBoard[a][b] != logicBoard[a-1][b] && logicBoard[a][b] != logicBoard[a+1][b] && logicBoard[a][b] != logicBoard[a][b-1] && logicBoard[a][b] != logicBoard[a][b+1]) createConnection(a,b,logicBoard[a][b]);;
                }
            }
        }

        //check which connections will die after last move and handle cases

        oneDies = doesOneDie();
        twoDies = doesTwoDie();

        if(!twoDies)
            if(oneDies)
                return false;
            else
                return true;
        else{
            killEnemies();
            return true;
        }
    }

    public void killEnemies(){

        for (int i=0;i<connections2.size();i++){
            String[] cords = connections2.get(i).split(";");
            for(int j=0;j<cords.length;j++){
                String[] xy = cords[j].split(",");
                if(hasBreaths(Integer.parseInt(xy[0]),Integer.parseInt(xy[1]))){
                    break;
                }
                if(j==cords.length-1){
                    updateBoards(i);
                }
            }

        }
    }

    public void updateBoards(int a){
        String[] cords = connections2.get(a).split(";");
        for(int i=0;i<cords.length;i++){
            String[] xy = cords[i].split(",");

            int x,y;
            x = Integer.parseInt(xy[0])-1;
            y = Integer.parseInt(xy[1])-1;

            board[x][y] = null;
            currentPlayer.adjustPlayersBoards(x,y);

        }
    }



    public boolean doesTwoDie(){

        for(String z : connections2){
            String[] cords = z.split(";");
            for(int i=0;i<cords.length;i++){
                String[] xy = cords[i].split(",");
                if(hasBreaths(Integer.parseInt(xy[0]),Integer.parseInt(xy[1]))){
                    break;
                }
                if(i+1==cords.length)
                    return true;
            }
        }
        return false;
    }

    public boolean doesOneDie(){

        for(String z : connections1){
            String[] cords = z.split(";");
            for(int i=0;i<cords.length;i++){
                String[] xy = cords[i].split(",");
                if(hasBreaths(Integer.parseInt(xy[0]),Integer.parseInt(xy[1]))){
                    break;
                }
                if(i+1==cords.length)
                    return true;
            }
        }
        return false;
    }

    public boolean hasBreaths(int a, int b){
        if(logicBoard[a-1][b] == 0 || logicBoard[a+1][b] == 0 || logicBoard[a][b-1] == 0 || logicBoard[a][b+1] == 0)
            return true;
        else
            return false;
    }

    public void createConnection(int a, int b,int c, int d, int type){

        if(type==1){
            for (int i=0;i<connections1.size();i++) {
                if(connections1.get(i).contains(a+","+b)&&!connections1.get(i).contains(c+","+d)){
                    connections1.set(i,connections1.get(i)+";"+c+","+d);
                    return;
                }else if(!connections1.get(i).contains(a+","+b)&&connections1.get(i).contains(c+","+d)){
                    connections1.set(i,connections1.get(i)+";"+a+","+b);
                    return;
                }else if(connections1.get(i).contains(a+","+b)&&connections1.get(i).contains(c+","+d)){
                    return;
                }
            }
            connections1.add(a+","+b+";"+c+","+d);
        }else if(type==2){
            for (int i=0;i<connections2.size();i++) {
                if(connections2.get(i).contains(a+","+b)&&!connections2.get(i).contains(c+","+d)){
                    connections2.set(i,connections2.get(i)+";"+c+","+d);
                    return;
                }else if(!connections2.get(i).contains(a+","+b)&&connections2.get(i).contains(c+","+d)){
                    connections2.set(i,connections2.get(i)+";"+a+","+b);
                    return;
                }else if(connections2.get(i).contains(a+","+b)&&connections2.get(i).contains(c+","+d)){
                    return;
                }
            }
            connections2.add(a+","+b+";"+c+","+d);
        }
    }

    public void createConnection(int a, int b, int type){
        if(type==1){
            connections1.add(a+","+b);
        }else if(type==2){
            connections2.add(a+","+b);
        }
    }

    private void setBoardSize(int size) {
        boardSize = size;
        board = new Player[size][size];
    }

    class Player implements Runnable {
        char pawn;
        Player opponent;
        Socket socket;
        Scanner input;
        PrintWriter output;

        public Player(Socket socket, char pawn) {
            this.socket = socket;
            this.pawn = pawn;
        }

        @Override
        public void run() {
            try {
                setup();
                processCommands();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (opponent != null && opponent.output != null) {
                    opponent.output.println("OTHER_PLAYER_LEFT");
                }
                try {socket.close();} catch (IOException e) {}
            }
        }

        private void setup() throws IOException {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("WELCOME " + pawn);
            if (pawn == 'B') {
                currentPlayer = this;
                output.println("MESSAGE Waiting for opponent to connect");
            } else {
                opponent = currentPlayer;
                opponent.opponent = this;
                opponent.output.println("MESSAGE Your move");
            }
        }

        private void processCommands() {
            while (input.hasNextLine()) {
                var command = input.nextLine();
                if (command.startsWith("QUIT")) {
                    return;
                } else if (command.startsWith("MOVE")) {
                    String[] cords = command.substring(5).split(";");
                    processMoveCommand(Integer.parseInt(cords[0]),Integer.parseInt(cords[1]));
                } else if (command.startsWith("SIZE")) {
                    setBoardSize(Integer.parseInt(command.substring(5)));
                }
            }
        }


        private void processMoveCommand(int row, int col) {
            try {
                move(row, col, this);
                output.println("VALID_MOVE");
                opponent.output.println("OPPONENT_MOVED " + row + ";" + col);

            } catch (IllegalStateException e) {
                output.println("MESSAGE " + e.getMessage());
            }
        }

        public void adjustPlayersBoards(int row, int col){
            output.println("MOVE " + row + ";" + col);
            opponent.output.println("MOVE " + row + ";" + col);
        }

    }
}