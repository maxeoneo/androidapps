package com.maxeoneo.rollingdice;

import android.content.Context;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * A container for OpenGL ES graphics. It also can handle touch events of the
 * user
 */
public class DiceGLSurfaceView extends GLSurfaceView {

	private final DiceRenderer mRenderer;

	public DiceGLSurfaceView(Context context) {
		super(context);

		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);

		// Set the Renderer for drawing on the GLSurfaceView
		mRenderer = new DiceRenderer(context);
		setRenderer(mRenderer);

		// Render the view only when there is a change in the drawing data
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {

		CastingThread ct = new CastingThread(getContext());
		ct.start();
		return true;
	}

	/**
	 * Method to return a CastingThread an external class
	 */
	public CastingThread getNewCastingThread() {
		return new CastingThread(getContext());
	}

	/**
	 * Thread which does the casting.
	 */
	public class CastingThread extends Thread {

		private long castingTime;
		private long start;
		private MediaPlayer loopSound;
		private MediaPlayer endSound;

		private CastingThread(Context context) {
			castingTime = (int) (2 + (Math.random() * (4 - 2))) * 1000;
			loopSound = MediaPlayer.create(context, R.raw.dice_loop);
			loopSound.setLooping(true);
			endSound = MediaPlayer.create(context, R.raw.dice_end);
			endSound.setLooping(false);
		}

		public void run() {
			// only cast if there is now other casting running
			if (!mRenderer.isCasting()) {

				// play first sound
				loopSound.start();

				mRenderer.setCasting(true);
				start = System.currentTimeMillis();

				// cast the dice
				while (System.currentTimeMillis() < start + castingTime) {

					mRenderer.setAngle((mRenderer.getAngle() + 10) % 360);
					mRenderer.setRotateX((float) Math.random());
					mRenderer.setRotateY((float) Math.random());
					requestRender();
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				// stop loop sound and start the end sound
				loopSound.stop();
				endSound.start();
				

				// bring dice in position that only one side is shown (mainly)
				float x = 90 - (mRenderer.getAlreadyRotatedX() % 90);
				float y = 90 - (mRenderer.getAlreadyRotatedY() % 90);
				
				// correct that axis that is next to 45 first
				if (Math.abs(x - 45) > Math.abs(y - 45)) {
					// bring x-achse in right position
					mRenderer.setAngle(mRenderer.getAngle() + x);
					mRenderer.setRotateX(1);
					mRenderer.setRotateY(0);
					requestRender();
					
				} else {
					// bring y-achse in right position
					mRenderer.setAngle(mRenderer.getAngle() + y);
					mRenderer.setRotateX(0);
					mRenderer.setRotateY(1);
					requestRender();
				}

				// set casting false so that a next casting can start
				mRenderer.setCasting(false);
			}
		}
	}
}
