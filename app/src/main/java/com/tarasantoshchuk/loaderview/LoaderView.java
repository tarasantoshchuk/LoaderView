package com.tarasantoshchuk.loaderview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Random;

public class LoaderView extends View {
    private static final int REDRAW_DELAY_MILLIS = 200;

    private boolean mIsEnabled = true;

    private ShapeDrawable mDrawable;
    private Quadrant mQuadrant;

    private int mWidth;
    private int mHeight;

    private Runnable mRedrawRunnable = new Runnable() {
        @Override
        public void run() {
            updateDrawable();
            invalidate();
        }
    };
    private static final Random mRandom = new Random();

    interface BoundsCalculator {
        int getTop(int width, int height);
        int getBottom(int width, int height);
        int getLeft(int width, int height);
        int getRight(int width, int height);
    }

    private enum Quadrant implements BoundsCalculator {
        TOP_LEFT(0xffff0000, new BoundsCalculator() {
            @Override
            public int getTop(int width, int height) {
                return 0;
            }

            @Override
            public int getBottom(int width, int height) {
                return height / 3;
            }

            @Override
            public int getLeft(int width, int height) {
                return 0;
            }

            @Override
            public int getRight(int width, int height) {
                return width / 2;
            }
        }),
        TOP_RIGHT(0xffffff00, new BoundsCalculator() {
            @Override
            public int getTop(int width, int height) {
                return 0;
            }

            @Override
            public int getBottom(int width, int height) {
                return height / 3;
            }

            @Override
            public int getLeft(int width, int height) {
                return width / 2;
            }

            @Override
            public int getRight(int width, int height) {
                return width;
            }
        }),
        MIDDLE_RIGHT(0xff00ff00, new BoundsCalculator() {
            @Override
            public int getTop(int width, int height) {
                return height / 3;
            }

            @Override
            public int getBottom(int width, int height) {
                return 2 * height / 3;
            }

            @Override
            public int getLeft(int width, int height) {
                return width / 2;
            }

            @Override
            public int getRight(int width, int height) {
                return width;
            }
        }),
        BOTTOM_RIGHT(0xff00ffff, new BoundsCalculator() {
            @Override
            public int getTop(int width, int height) {
                return 2 * height / 3;
            }

            @Override
            public int getBottom(int width, int height) {
                return height;
            }

            @Override
            public int getLeft(int width, int height) {
                return width / 2;
            }

            @Override
            public int getRight(int width, int height) {
                return width;
            }
        }),
        BOTTOM_LEFT(0xff0000ff, new BoundsCalculator() {
            @Override
            public int getTop(int width, int height) {
                return 2 * height / 3;
            }

            @Override
            public int getBottom(int width, int height) {
                return height;
            }

            @Override
            public int getLeft(int width, int height) {
                return 0;
            }

            @Override
            public int getRight(int width, int height) {
                return width / 2;
            }
        }),
        MIDDLE_LEFT(0xffff00ff, new BoundsCalculator() {
            @Override
            public int getTop(int width, int height) {
                return height / 3;
            }

            @Override
            public int getBottom(int width, int height) {
                return 2 * height / 3;
            }

            @Override
            public int getLeft(int width, int height) {
                return 0;
            }

            @Override
            public int getRight(int width, int height) {
                return width / 2;
            }
        });

        private int mColor;
        private BoundsCalculator mCalculator;

        Quadrant(int color, BoundsCalculator calculator) {
            mColor = color;
            mCalculator = calculator;
        }

        public static Quadrant getNext(@NonNull Quadrant current) {
            switch (current) {
                case TOP_LEFT:
                    return TOP_RIGHT;
                case TOP_RIGHT:
                    return MIDDLE_RIGHT;
                case MIDDLE_RIGHT:
                    return BOTTOM_RIGHT;
                case BOTTOM_RIGHT:
                    return BOTTOM_LEFT;
                case BOTTOM_LEFT:
                    return MIDDLE_LEFT;
                case MIDDLE_LEFT:
                    return TOP_LEFT;
                default:
                    throw new RuntimeException("unexpected quadrant");
            }
        }

        public static Quadrant getRandomQuadrant(Quadrant current) {
            int maxIndex = Quadrant.values().length;

            int randomIndex;
            Quadrant result;
            do {
                randomIndex = mRandom.nextInt(maxIndex);
                result = Quadrant.values()[randomIndex];
            } while (current == result);

            return result;
        }

        @Override
        public int getTop(int width, int height) {
            return mCalculator.getTop(width, height);
        }

        @Override
        public int getBottom(int width, int height) {
            return mCalculator.getBottom(width, height);
        }

        @Override
        public int getLeft(int width, int height) {
            return mCalculator.getLeft(width, height);
        }

        @Override
        public int getRight(int width, int height) {
            return mCalculator.getRight(width, height);
        }
    }

    public LoaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mQuadrant = Quadrant.BOTTOM_LEFT;
        mDrawable = new ShapeDrawable(new RectShape());
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsEnabled = !mIsEnabled;

                if(mIsEnabled) {
                    post(mRedrawRunnable);
                } else {
                    removeCallbacks(mRedrawRunnable);
                }
            }
        });
    }

    public LoaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoaderView(Context context) {
        this(context, null);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mWidth = right - left;
        mHeight = bottom - top;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.v("LoaderView", "onDraw");
        mDrawable.draw(canvas);

        mQuadrant = Quadrant.getNext(mQuadrant);

        if(mIsEnabled) {
            postDelayed(mRedrawRunnable, REDRAW_DELAY_MILLIS);
        }
    }

    private void updateDrawable() {
        mDrawable = new ShapeDrawable(mRandom.nextBoolean() ? new OvalShape() : new RectShape());
        mDrawable.getPaint().setColor(mQuadrant.mColor);
        mDrawable.setBounds(
                mQuadrant.getLeft(mWidth, mHeight),
                mQuadrant.getTop(mWidth, mHeight),
                mQuadrant.getRight(mWidth, mHeight),
                mQuadrant.getBottom(mWidth, mHeight)
        );
    }
}