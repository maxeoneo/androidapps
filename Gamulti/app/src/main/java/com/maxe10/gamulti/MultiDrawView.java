package com.maxe10.gamulti;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;

/**
 * Created by maxe10 on 09/05/2017.
 */

public class MultiDrawView extends View
{
  private static boolean mDrawMode = true;

  private static final int CIRCLE_SIZE = 75;
  private static final int MAX_DELTA_ALLOWED = 100;

  private GestureDetector gestureDetector;

  private SparseArray<PointF> mActivePointers;
  private SparseArray<Path> mActivePaths;

  private HashSet<Path> mTemplatePaths;
  private HashSet<Path> mCorrectPaths;
  private HashSet<Path> mWrongPaths;

  private Paint mPaint;
  private int[] colors = { Color.BLUE, Color.CYAN, Color.GRAY, Color.DKGRAY, Color.LTGRAY, Color.YELLOW, Color.MAGENTA };

  private Paint textPaint;

  public MultiDrawView(Context context, AttributeSet attrs)
  {
    super(context);
    gestureDetector = new GestureDetector(context, new GestureListener());
    initView();
  }

  private void initView()
  {
    mActivePointers = new SparseArray<PointF>();
    mActivePaths = new SparseArray<Path>();
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // set painter color to a color you like
    mPaint.setColor(Color.BLUE);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeWidth(6f);

    mCorrectPaths = new HashSet<Path>();
    mWrongPaths = new HashSet<Path>();


    textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    textPaint.setTextSize(20);

    createTemplatePaths();
  }

  private void createTemplatePaths()
  {
    mTemplatePaths = new HashSet<Path>();

    Path tempPath1 = new Path();
    tempPath1.moveTo(100, 100);
    tempPath1.lineTo(200, 1600);
    mTemplatePaths.add(tempPath1);

    Path tempPath2 = new Path();
    tempPath2.moveTo(800, 800);
    tempPath2.lineTo(800, 250);
    mTemplatePaths.add(tempPath2);
  }



  private boolean comparePaths()
  {
    // create new set of paths containing all path which should be compared with the drawn paths
    HashSet<Path> templatePathsToCompare = new HashSet<Path>();
    for (Path tempPath : mTemplatePaths)
    {
      templatePathsToCompare.add(tempPath);
    }

    for (int i = 0; i < mActivePaths.size(); i++)
    {
      Path correspondingTemplatePath = null;
      Path userPath = mActivePaths.get(i);

      for (Path templatePath : templatePathsToCompare)
      {
        if (comparePaths(userPath, templatePath))
        {
          correspondingTemplatePath = templatePath;
          break;
        }
      }

      if (correspondingTemplatePath != null)
      {
        System.out.println("Paths are nearly equals");
        mCorrectPaths.add(userPath);
        templatePathsToCompare.remove(correspondingTemplatePath);
      }
      else
      {
        System.out.println("No corresponding path found");
        mWrongPaths.add(userPath);
      }
    }

    if (!templatePathsToCompare.isEmpty() && mWrongPaths.isEmpty())
    {
      System.out.println("You did not draw enough paths");
    }

    return mWrongPaths.isEmpty() && templatePathsToCompare.isEmpty();
  }

  @Nullable
  private Boolean comparePaths(Path userPath, Path templatePath)
  {
    PathMeasure templatePathMeasure = new PathMeasure(templatePath, false);
    PathMeasure pathMeasure = new PathMeasure(userPath, false);
    float fDistance = 0.0f;

    float fSmallerLength = Math.min(templatePathMeasure.getLength(), pathMeasure.getLength());
    float fBiggerLength = Math.max(templatePathMeasure.getLength(), pathMeasure.getLength());

    if (fBiggerLength - fSmallerLength > MAX_DELTA_ALLOWED)
    {
      System.out.println("NOT EQUALS: ONE PATH IS LONGER");
      return false;
    }

    float[] posTemplate = new float[2];
    float[] posPath = new float[2];
    float[] tanTemplate = new float[2];
    float[] tanPath = new float[2];

    while (fDistance <= fSmallerLength)
    {
      templatePathMeasure.getPosTan(fDistance, posTemplate, tanTemplate);
      pathMeasure.getPosTan(fDistance, posPath, tanPath);

      float fDiffX = Math.abs(posTemplate[0] - posPath[0]);
      float fDiffY = Math.abs(posTemplate[1] - posPath[1]);
      double diffPos = Math.sqrt(fDiffX * fDiffX + fDiffY * fDiffY);

      if (diffPos > MAX_DELTA_ALLOWED)
      {
        System.out.println("NOT EQUALS: PATH ARE NOT CLOSE ENOUGH");
        return false;
      }

      fDistance += 1.0f;
    }
    return true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    if (!mDrawMode)
    {
      // delegate the event to the gesture detector
      return gestureDetector.onTouchEvent(event);
    }

    // get pointer index from the event object
    int pointerIndex = event.getActionIndex();

    // get pointer ID
    int pointerId = event.getPointerId(pointerIndex);

    // get masked (not specific to a pointer) action
    int maskedAction = event.getActionMasked();

    switch (maskedAction)
    {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        // we have a new pointer. Lets add it to the list of pointers

        PointF f = new PointF();
        f.x = event.getX(pointerIndex);
        f.y = event.getY(pointerIndex);
        mActivePointers.put(pointerId, f);

        Path newPath = new Path();
        newPath.moveTo(f.x, f.y);
        mActivePaths.put(pointerId, newPath);
        break;

      case MotionEvent.ACTION_MOVE:
        for (int i = 0; i < event.getPointerCount(); i++)
        {
          PointF point = mActivePointers.get(event.getPointerId(i));
          if (point != null)
          {
            point.x = event.getX(i);
            point.y = event.getY(i);
          }
          Path path = mActivePaths.get(event.getPointerId(i));
          if (path != null)
          {
            path.lineTo(event.getX(i), event.getY(i));
          }
        }
        break;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
      case MotionEvent.ACTION_CANCEL:

        comparePaths();
        mActivePointers.remove(pointerId);
        mActivePaths.remove(pointerId);

        if (mActivePointers.size() == 0)
        {
          mDrawMode = false;
        }
        break;
    }

    invalidate();
    return true;
  }

  @Override
  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);

    // draw all pointers
    for (int i = 0; i < mActivePointers.size(); i++)
    {
      mPaint.setColor(colors[i % 9]);

      PointF point = mActivePointers.valueAt(i);
      if (point != null)
      {
        canvas.drawCircle(point.x, point.y, CIRCLE_SIZE, mPaint);
      }

      Path path = mActivePaths.valueAt(i);
      if (path != null)
      {
        canvas.drawPath(path, mPaint);
      }
      canvas.drawText("Total pointers: " + mActivePointers.size(), 10, 40, textPaint);
    }

    // draw template paths
    mPaint.setColor(Color.BLACK);
    for (Path p : mTemplatePaths)
    {
      canvas.drawPath(p, mPaint);
    }

    // draw correct paths
    mPaint.setColor(Color.GREEN);
    for (Path p : mCorrectPaths)
    {
      canvas.drawPath(p, mPaint);
    }

    // draw wrong paths
    mPaint.setColor(Color.RED);
    for (Path p : mWrongPaths)
    {
      if (p != null)
      {
        canvas.drawPath(p, mPaint);
      }
    }
  }

  private class GestureListener extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onDoubleTap(MotionEvent e)
    {
      float x = e.getX();
      float y = e.getY();

      // reset
      MultiDrawView.mDrawMode = true;
      mActivePaths.clear();
      mActivePointers.clear();
      mCorrectPaths.clear();
      mWrongPaths.clear();

      invalidate();

      Log.d("Double Tap", "Tapped at: (" + x + ", " + y + ")");

      return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
      super.onLongPress(e);

    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
      return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
      return true;
    }
  }
}
