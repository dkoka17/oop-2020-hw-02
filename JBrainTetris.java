import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class JBrainTetris extends JTetris{

    private JSlider adversary;
    private JLabel status;
    private DefaultBrain db;
    private JCheckBox brainMode;


    public JBrainTetris(int pixels){
        super(pixels);
        db = new DefaultBrain();

    }

    @Override
    public JComponent createControlPanel(){
        JPanel panel = (JPanel) super.createControlPanel();
        panel.add(new JLabel("Brain:"));
        brainMode = new JCheckBox("Brain active");
        panel.add(brainMode);

        JPanel little = new JPanel();
        little.add(new JLabel("Adversary:"));
        adversary = new JSlider(0, 100, 0); // min, max, current
        adversary.setPreferredSize(new Dimension(100,15));

        status = new JLabel("ok");
        little.add(status);
        little.add(adversary);

        panel.add(little);
        return panel;
    }

    @Override
    public Piece pickNextPiece() {
        int rand = (new Random()).nextInt(100);
        if(rand >= adversary.getValue()){
            Piece pc = super.pickNextPiece();
            status.setText("ok");
            return pc;

        }
        status.setText("*ok*");
        Piece[] pieces = Piece.getPieces();
        Piece worstPiece = pieces[0];
        double worstScore = Double.MIN_VALUE;
        for(Piece p: pieces){
            Brain.Move move2 = new Brain.Move();
            db.bestMove(board, p, board.getHeight(), move2);
            if(move2 != null & move2.score > worstScore){
                worstPiece = p;
                worstScore = move2.score;
            }
        }
        return worstPiece;
    }
    private int lasC=0;
    private  Brain.Move move2;
    @Override
    public void tick(int verb){

        if(verb==ROTATE){
            super.tick(verb);
            super.tick(DOWN);
            return;
        }
        if(brainMode.isSelected()){
            board.undo();
            move2 = db.bestMove(board, currentPiece, board.getHeight(), move2);
            if(move2==null){
                return;
            }
            if(!currentPiece.equals(move2.piece)){
                super.tick(ROTATE);
            }
            if(move2.x>currentX){
                super.tick(RIGHT);
            }else if(move2.x<currentX){
                super.tick(LEFT);
            }
            super.tick(DOWN);
        }else{
            if( verb != DOWN){
                super.tick(verb);
            }
            super.tick(DOWN);
        }

    }

    public static void main(String[] args){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        JBrainTetris jbt = new JBrainTetris(16);
        JFrame frame = jbt.createFrame(jbt);
        frame.setVisible(true);
    }
}
