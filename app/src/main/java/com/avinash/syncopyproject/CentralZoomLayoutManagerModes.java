package com.avinash.syncopyproject;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CentralZoomLayoutManagerModes extends LinearLayoutManager {

    private final float mShrinkAmount = 0.5f;
    private final float mShrinkDistance = 1f;
    private float childMidPoint;

    public CentralZoomLayoutManagerModes(Context context) {
        super(context);
    }

    public CentralZoomLayoutManagerModes(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int orientation = getOrientation();
        if (orientation == HORIZONTAL) {
            int scrolled = super.scrollHorizontallyBy(dx, recycler, state);

            float midpoint = getWidth() / 2.f;
            float d0 = 0.f;
            float d1 = mShrinkDistance * midpoint;
            float s0 = 1.f;
            float s1 = 1.f - mShrinkAmount;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
//                    float childMidpoint =
//                            (getDecoratedRight(child) + getDecoratedLeft(child)) / 2.f;
                childMidPoint = child.getX() + child.getWidth() / 2.0f;

                float d = Math.min(d1, Math.abs(midpoint - childMidPoint));
                float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
                child.setScaleX(scale);
                child.setScaleY(scale);
            }
            return scrolled;
        } else {
            return 0;
        }

    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);

        //aqui ele executa o codigo no estado inicial, originalmente ele nao aplicava no inicio
        //este codigo é em horizontal. Para usar em vertical apagar e copiar o codigo
        int orientation = getOrientation();
        if (orientation == HORIZONTAL) {


            float midpoint = getWidth() / 2.f;
            float d0 = 0.f;
            float d1 = mShrinkDistance * midpoint;
            float s0 = 1.f;
            float s1 = 1.f - mShrinkAmount;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
//                    float childMidpoint =
//                            (getDecoratedRight(child) + getDecoratedLeft(child)) / 2.f;
                childMidPoint = child.getX() + child.getWidth() / 2.0f;
                float d = Math.min(d1, Math.abs(midpoint - childMidPoint));
                float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
                child.setScaleX(scale);
                child.setScaleY(scale);
            }

        } else {

            float midpoint = getHeight() / 2.f;
            float d0 = 0.f;
            float d1 = mShrinkDistance * midpoint;
            float s0 = 1.f;
            float s1 = 1.f - mShrinkAmount;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                float childMidpoint =
                        (getDecoratedBottom(child) + getDecoratedTop(child)) / 2.f;
                float d = Math.min(d1, Math.abs(midpoint - childMidpoint));
                float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
                child.setScaleX(scale);
                child.setScaleY(scale);
            }

        }
    }

}
