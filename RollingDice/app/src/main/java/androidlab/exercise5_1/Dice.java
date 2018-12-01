package androidlab.exercise5_1;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;


/**
 * A Dice with dots and boarder lines in 3 Dimensions
 */
public class Dice {

	// colors
	private final static float[] BLACK = new float[] { 0.0f, 0.0f, 0.0f, 1.0f};
	private final static float[] WHITE = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	private final static float[] GREEN = new float[] { 0.0f, 1.0f, 0.0f, 1.0f};
	private final static float[] RED = new float[] { 1.0f, 0.0f, 0.0f, 1.0f};
	private final static float[] BLUE = new float[] { 0.0f, 0.0f, 1.0f, 1.0f};
	private final static float[] YELLOW = new float[] { 0.9f, 0.9f, 0.5f, 1.0f};
	private final static float[] PURPLE = new float[] { 1.0f, 0.0f, 1.0f, 1.0f};
	private final static float[] AQUA = new float[] { 0.0f, 1.0f, 1.0f, 1.0f};
	
	private final float sqrRootTwo = (float)Math.sqrt(2);
	
	// Code for the vertex shader
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // The matrix must be included as a modifier of gl_Position.
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";
    
    // Code for the fragment shader
    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
 
    static float cubeCoords[] = {
    		// FRONT
            -0.5f,  0.5f, 0.5f,   // top left
            -0.5f, -0.5f, 0.5f,   // bottom left
             0.5f, -0.5f, 0.5f,   // bottom right
             0.5f,  0.5f, 0.5f,   // top right

    		//BACK
             -0.5f,  0.5f, -0.5f,   // top left
             -0.5f, -0.5f, -0.5f,   // bottom left
              0.5f, -0.5f, -0.5f,   // bottom right
              0.5f,  0.5f, -0.5f};  // top right  
    		
 	
    
    
    private final short drawOrder[] = {
            0,1,2, 0,2,3, //Front
			2,3,6, 3,6,7, //Right
			4,5,6, 4,6,7, //Back
			0,1,5, 0,4,5, //LEFT
			0,3,4, 3,4,7, //Top
			1,2,5, 2,5,6  //Bottom
			};
    
    // boarder lines of the cube
   	private Line[] boarders = new Line[12];
   	
   	// dots of the dice
   	private Circle[] dotsOfDice = new Circle[21];

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private float cubeColor[] = WHITE;
    private float boarderColor[] = BLACK;

    
    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Dice() {
        // create boarders and dots
    	createBoarders();
    	createDots();
    	
    	// initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
 
        // load shaders
        int vertexShader = DiceRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = DiceRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        
        // create OpenGL Program
        mProgram = GLES20.glCreateProgram();
        
        // add the vertex and fragment shader to program
        GLES20.glAttachShader(mProgram, vertexShader);   
        GLES20.glAttachShader(mProgram, fragmentShader); 
        
        // create program executables
        GLES20.glLinkProgram(mProgram);                  
    }

    /**
     * draw the dice with dots and boarder lines
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {

    	// Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the cube
        GLES20.glUniform4fv(mColorHandle, 1, cubeColor, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        DiceRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        DiceRenderer.checkGlError("glUniformMatrix4fv");
        
        // Draw the cube
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

       
        // Draw the dots of the dice
        for (Circle dot : dotsOfDice) {
        	if (dot != null) {
        		dot.draw(mvpMatrix);
        	}
        }
        
        
        // draw the boarders
        for (int i = 0; i < boarders.length; i++) {
        	boarders[i].draw(mvpMatrix);
        }

        
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
    
    /**
     * Method to create the boardes of the cube
     */
    private void createBoarders() {
    	// front
    	boarders[0] = new Line(new float[]{-0.501f, 0.501f, 0.501f, -0.501f, -0.501f, 0.501f}, boarderColor);
    	boarders[1] = new Line(new float[]{-0.501f, -0.501f, 0.501f, 0.501f, -0.501f, 0.501f}, boarderColor);
    	boarders[2] = new Line(new float[]{0.501f, -0.501f, 0.501f, 0.501f, 0.501f, 0.501f}, boarderColor);
    	boarders[3] = new Line(new float[]{-0.501f, 0.501f, 0.501f, 0.501f, 0.501f, 0.501f}, boarderColor);
    	// back
    	boarders[4] = new Line(new float[]{-0.501f, 0.501f, -0.501f, -0.501f, -0.501f, -0.501f}, boarderColor);
    	boarders[5] = new Line(new float[]{-0.501f, -0.501f, -0.501f, 0.5f, -0.501f, -0.501f}, boarderColor);
    	boarders[6] = new Line(new float[]{0.501f, -0.501f, -0.501f, 0.501f, 0.501f, -0.501f}, boarderColor);
    	boarders[7] = new Line(new float[]{-0.501f, 0.501f, -0.501f, 0.501f, 0.501f, -0.501f}, boarderColor);
    	// left
    	boarders[8] = new Line(new float[]{-0.501f, 0.501f, 0.501f, -0.501f, 0.501f, -0.501f}, boarderColor);
    	boarders[9] = new Line(new float[]{-0.501f, -0.501f, 0.501f, -0.501f, -0.501f, -0.501f}, boarderColor);
    	//right
    	boarders[10] = new Line(new float[]{0.501f, 0.501f, 0.501f, 0.501f, 0.501f, -0.501f}, boarderColor);
    	boarders[11] = new Line(new float[]{0.501f, -0.501f, 0.501f, 0.501f, -0.501f, -0.501f}, boarderColor);
    }
    
    /**
     * Method to create the dots of the dice
     */
    private void createDots() {

        float radius = 0.05f;
        
        // dot in Front
        dotsOfDice[0] = dotsOfFrontAndBack(0, 0, 0.51f, radius, RED);
        
        // dots at top
        dotsOfDice[1] = dotsOfTopAndBottom(-0.25f, 0.51f, 0.25f, radius, GREEN);
        dotsOfDice[2] = dotsOfTopAndBottom(0.25f, 0.51f, -0.25f, radius, GREEN);
        
        // dots left
        dotsOfDice[3] = dotsOfLeftAndRight(-0.51f, 0.25f, 0.25f, radius, BLUE);
        dotsOfDice[4] = dotsOfLeftAndRight(-0.51f, 0.0f, 0.0f, radius, BLUE);
        dotsOfDice[5] = dotsOfLeftAndRight(-0.51f, -0.25f, -0.25f, radius, BLUE);
        
        //dots right
        dotsOfDice[6] = dotsOfLeftAndRight(0.51f, 0.25f, 0.25f, radius, YELLOW);
        dotsOfDice[7] = dotsOfLeftAndRight(0.51f, 0.25f, -0.25f, radius, YELLOW);
        dotsOfDice[8] = dotsOfLeftAndRight(0.51f, -0.25f, 0.25f, radius, YELLOW);
        dotsOfDice[9] = dotsOfLeftAndRight(0.51f, -0.25f, -0.25f, radius, YELLOW);
        
        
        // dots at bottom
        dotsOfDice[10] = dotsOfTopAndBottom(-0.0f, -0.51f, 0.0f, radius, PURPLE);
        dotsOfDice[11] = dotsOfTopAndBottom(-0.25f, -0.51f, -0.25f, radius, PURPLE);
        dotsOfDice[12] = dotsOfTopAndBottom(-0.25f, -0.51f, 0.25f, radius, PURPLE);
        dotsOfDice[13] = dotsOfTopAndBottom(0.25f, -0.51f, -0.25f, radius, PURPLE);
        dotsOfDice[14] = dotsOfTopAndBottom(0.25f, -0.51f, 0.25f, radius, PURPLE);
        
        //dots at Back
        dotsOfDice[15] = dotsOfFrontAndBack(-0.25f, -0.25f, -0.51f, radius, AQUA);
        dotsOfDice[16] = dotsOfFrontAndBack(-0.25f, -0.0f, -0.51f, radius, AQUA);
        dotsOfDice[17] = dotsOfFrontAndBack(-0.25f, 0.25f, -0.51f, radius, AQUA);
        dotsOfDice[18] = dotsOfFrontAndBack(0.25f, -0.25f, -0.51f, radius, AQUA);
        dotsOfDice[19] = dotsOfFrontAndBack(0.25f, -0.0f, -0.51f, radius, AQUA);
        dotsOfDice[20] = dotsOfFrontAndBack(0.25f, 0.25f, -0.51f, radius, AQUA);
    }
    
    /**
     * create the circles using the coords of a point in the middle and a radius.
     * here the z-coords doesn't change
     */
    private Circle dotsOfFrontAndBack(float x, float y, float z, float radius, float[] color) {
    	
        float[] coords = new float[] {
        		x, y, z,													//Center 
        		x, y + radius, z,											//Top
        		x + (radius / sqrRootTwo), y + (radius / sqrRootTwo), z,	//Top-Right
        		x + radius, y, z, 											//Right
        		x + (radius / sqrRootTwo), y - (radius / sqrRootTwo), z, 	//Bottom-Right
        		x, y - radius, z, 											//Bottom
        		x - (radius / sqrRootTwo), y - (radius / sqrRootTwo), z, 	//Bottom-Left
        		x - radius, y, z,											//Left
        		x - (radius / sqrRootTwo), y + (radius / sqrRootTwo), z, 	//Top-Left
        };
        return new Circle(coords, color);
    }
    
    /**
     * create the circles using the coords of a point in the middle and a radius.
     * here the x-coords doesn't change
     */
    private Circle dotsOfLeftAndRight (float x, float y, float z, float radius, float[] color) {
    	
        float[] coords = new float[] {
        		x, y, z,													//Center 
        		x, y + radius, z,											//Top
        		x, y + (radius / sqrRootTwo), z + (radius / sqrRootTwo),	//Top-Right
        		x, y, z + radius, 											//Right
        		x, y - (radius / sqrRootTwo), z + (radius / sqrRootTwo), 	//Bottom-Right
        		x, y - radius, z, 											//Bottom
        		x, y - (radius / sqrRootTwo), z - (radius / sqrRootTwo), 	//Bottom-Left
        		x, y, z - radius,											//Left
        		x, y + (radius / sqrRootTwo), z - (radius / sqrRootTwo), 	//Top-Left
        };
        return new Circle(coords, color);
    }
    
    /**
     * create the circles using the coords of a point in the middle and a radius.
     * here the y-coords doesn't change
     */
    private Circle dotsOfTopAndBottom (float x, float y, float z, float radius, float[] color) {
    	
        float[] coords = new float[] {
        		x, y, z,													//Center 
        		x, y, z + radius,											//Top
        		x + (radius / sqrRootTwo), y, z + (radius / sqrRootTwo),	//Top-Right
        		x + radius, y, z, 											//Right
        		x + (radius / sqrRootTwo), y, z - (radius / sqrRootTwo), 	//Bottom-Right
        		x, y, z - radius, 											//Bottom
        		x - (radius / sqrRootTwo), y, z - (radius / sqrRootTwo), 	//Bottom-Left
        		x - radius, y, z,											//Left
        		x - (radius / sqrRootTwo), y, z + (radius / sqrRootTwo), 	//Top-Left
        };
        return new Circle(coords, color);
    }
}