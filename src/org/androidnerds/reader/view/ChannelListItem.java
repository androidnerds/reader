/*
 * Copyright (C) 2007-2010 Michael Novak <mike@androidnerds.org>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package org.androidnerds.reader.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import org.androidnerds.reader.R;
import org.androidnerds.reader.activity.ChannelList.ChannelListAdapter;

public class ChannelListItem extends RelativeLayout {
	
	private static final String TAG = "ChannelListItem";
	
	private ChannelListAdapter mAdapter;
	private boolean mAllowBatch;
	private boolean mCachedViewPositions;
	
	private boolean mDownEvent;
	private int mCheckRight;
	private int mStarLeft;
	
	private static final float CLICKABLE_PAD = 10.0f;
	
	public boolean mFavorite;
	public boolean mSelected;
	public boolean mRead;
	
	public ChannelListItem(Context context) {
		super(context);
	}
	
	public ChannelListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ChannelListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void bindViewInit(ChannelListAdapter adapter, boolean allowBatch) {
		mAdapter = adapter;
		mAllowBatch = allowBatch;
		mCachedViewPositions = false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = false;
		int touchX = (int) event.getX();
		
		if (!mCachedViewPositions) {
			float paddingScale = getContext().getResources().getDisplayMetrics().density;
			int clickPadding = (int) ((CLICKABLE_PAD * paddingScale) + 0.5);
			mCheckRight = findViewById(R.id.selected).getRight() + clickPadding;
			mStarLeft = findViewById(R.id.favorite).getLeft() - clickPadding;
			
			mCachedViewPositions = true;
		}
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownEvent = true;
			
			if ((mAllowBatch && touchX < mCheckRight) || touchX > mStarLeft) {
				handled = true;
			}
			
			break;
		case MotionEvent.ACTION_CANCEL:
			mDownEvent = false;
			break;
		case MotionEvent.ACTION_UP:
			if (mDownEvent) {
				if (mAllowBatch && touchX < mCheckRight) {
					mSelected = !mSelected;
					mAdapter.updateSelected(this, mSelected);
					handled = true;
				} else if (touchX > mStarLeft) {
					mFavorite = !mFavorite;
					mAdapter.updateFavorite(this, mFavorite);
					handled = true;
				}
			}
			
			break;
		}
		
		if (handled) {
			postInvalidate();
		} else {
			handled = super.onTouchEvent(event);
		}
		
		return handled;
	}
	
}