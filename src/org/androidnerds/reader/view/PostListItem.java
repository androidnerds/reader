/* Copyright (C) 2009, 2010 Android Nerds Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidnerds.reader.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import org.androidnerds.reader.R;
import org.androidnerds.reader.activity.PostList.PostListAdapter;

public class PostListItem extends RelativeLayout {
	
	private static final String TAG = "PostListItem";
	
	private PostListAdapter mAdapter;
	private boolean mAllowBatch;
	private boolean mCachedViewPositions;
	
	private boolean mDownEvent;
	private int mCheckRight;
	private int mStarLeft;
	
	private static final float CLICKABLE_PAD = 10.0f;
	
	public boolean mFavorite;
	public boolean mSelected;
	public boolean mRead;
	public long mPostId;
	
	public PostListItem(Context context) {
		super(context);
	}
	
	public PostListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public PostListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void bindViewInit(PostListAdapter adapter, boolean allowBatch) {
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
			mCheckRight = findViewById(R.id.selected_post).getRight() + clickPadding;
			mStarLeft = findViewById(R.id.favorite_post).getLeft() - clickPadding;
			
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