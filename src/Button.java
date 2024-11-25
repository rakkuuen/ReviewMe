import java.awt.*;


class Button extends Rectangle{
    int sizeX;
    int sizeY;
    String text;
    Color mainColor;
    Color hoverColor;

    public Button(int sizeX, int sizeY, String text){
        super(0, 0, sizeX, sizeY);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.text = text;
        mainColor = Color.BLUE;
        hoverColor = Color.RED;
    }
    
    void paint(Graphics g, Point mousePos){

        // Hover Colour
        if(contains(mousePos)){
            g.setColor(hoverColor);
        } else {
            g.setColor(mainColor);
        }
        g.fillRect(x, y, sizeX, sizeY);

        // Button border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, sizeX, sizeY);

         // Draw button text
         g.setColor(Color.WHITE);
         g.setFont(new Font("Arial", Font.BOLD, 18));
         FontMetrics fm = g.getFontMetrics();
         int textWidth = fm.stringWidth(text);
         int textHeight = fm.getAscent();
         int textX = x + (sizeX - textWidth) / 2;
         int textY = y + (sizeY + textHeight) / 2 - 4; // Adjust for baseline
         g.drawString(text, textX, textY);
    }
}
