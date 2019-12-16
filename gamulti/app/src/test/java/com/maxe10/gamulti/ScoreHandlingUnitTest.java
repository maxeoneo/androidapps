package com.maxe10.gamulti;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by alme on 11.05.17.
 */

public class ScoreHandlingUnitTest
{
  @Test
  public void checkPositionsCloseEnough_isCorrect() throws Exception
  {
    float[] pos1 = {0, 0};
    float[] pos2 = {100, 100};
    assertFalse(ScoreHandling.checkPositionsCloseEnough(pos1, pos2));

    float[] pos3 = {50, 50};
    assertTrue(ScoreHandling.checkPositionsCloseEnough(pos1, pos3));
  }
}
