package androidlab.exercise5_1;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Renderer for drawing objects.
 */
public class DiceRenderer implements GLSurfaceView.Renderer {

	private boolean casting = false;
	private static final String TAG = "MyGLRenderer";
	private Dice mCube;

	// mMVPMatrix is an abbreviation for "Model View Projection Matrix"
	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mViewMatrix = new float[16];
	private final float[] mRotationMatrix = new float[16];

	private float mAngle;
	// rotation for each axis
	private float rotateX = 1.0f;
	private float rotateY = 1.0f;
	
	private float alreadyRotatedX = 0.0f;
	private float alreadyRotatedY = 0.0f;

	public DiceRenderer(Context context) {
		super();
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {

		// Set the background frame color
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

		mCube = new Dice();
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		
		// No culling of back faces
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		 
		// No depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		 
		// Enable blending
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		
		float[] scratch = new float[16];

		// Draw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// Set the camera position (View matrix)
		Matrix.setLookAtM(mViewMatrix, 0, 0.8f, 0.8f, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		// Matrix.setLookAtM(mViewMatrix, 0, 1, 1, 1, 0f, 0f, 0f, 0f, 1.0f,
		// 0.0f);

		// Calculate the projection and view transformation
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

		// Create a rotation for the triangle random rotation
		Matrix.setRotateM(mRotationMatrix, 0, mAngle, rotateX, rotateY, 0);

		// Combine the rotation matrix with the projection and camera view
		// Note that the mMVPMatrix factor *must be first* in order
		// for the matrix multiplication product to be correct.
		Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
		
		// calculate the rotation of the x-, y-axis which is already done 
		// this is needed to bring the dice in a good position at the end
		float sum = rotateX + rotateY;
		alreadyRotatedX += (mAngle * (rotateX / sum));
		alreadyRotatedY += (mAngle * (rotateY / sum));
		alreadyRotatedX = alreadyRotatedX % 90;
		alreadyRotatedY = alreadyRotatedY % 90;
		
		// Draw cube
		mCube.draw(scratch);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		// Adjust the viewport based on geometry changes,
		// such as screen rotation
		GLES20.glViewport(0, 0, width, height);

		float ratio = (float) width / height;

		// this projection matrix is applied to object coordinates
		// in the onDrawFrame() method
		// Matrix.frustumM(m, offset, left, right, bottom, top, near, far)
		Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 2, 7);

	}

	/**
	 * load the shader with given type and shadercode
	 */
	public static int loadShader(int type, String shaderCode) {

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	/**
	 * method which provide some debugging possibility.
	 */
	public static void checkGlError(String glOperation) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, glOperation + ": glError " + error);
			throw new RuntimeException(glOperation + ": glError " + error);
		}
	}

	/**
	 * getter for the rotation angle
	 */
	public float getAngle() {
		return mAngle;
	}

	/**
	 * setter for the rotation angle
	 */
	public void setAngle(float angle) {
		mAngle = angle;
	}

	public boolean isCasting() {
		return casting;
	}

	public void setCasting(boolean casting) {
		this.casting = casting;
	}

	public float getRotateX() {
		return rotateX;
	}

	public void setRotateX(float rotateX) {
		this.rotateX = rotateX;
	}

	public float getRotateY() {
		return rotateY;
	}

	public void setRotateY(float rotateY) {
		this.rotateY = rotateY;
	}

	public float getAlreadyRotatedX() {
		return alreadyRotatedX;
	}

	public float getAlreadyRotatedY() {
		return alreadyRotatedY;
	}
}