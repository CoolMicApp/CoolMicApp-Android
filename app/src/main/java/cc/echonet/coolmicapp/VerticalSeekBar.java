/*
 * Copyright (C) 2019 Jörg Eisfeld
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
package cc.echonet.coolmicapp;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

/**
 * Implementation of an easy vertical SeekBar, based on the normal SeekBar.
 */
public class VerticalSeekBar extends SeekBar {
	/**
	 * The angle by which the SeekBar view should be rotated.
	 */
	private static final int ROTATION_ANGLE = -90;

	/**
	 * A change listener registrating start and stop of tracking. Need an own listener because the listener in SeekBar
	 * is private.
	 */
	private OnSeekBarChangeListener mOnSeekBarChangeListener;

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @see android.view.View#View(Context)
	 */
	public VerticalSeekBar(final Context context) {
		super(context);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @param attrs The attributes of the XML tag that is inflating the view.
	 * @see android.view.View#View(Context, AttributeSet)
	 */
	public VerticalSeekBar(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @param attrs The attributes of the XML tag that is inflating the view.
	 * @param defStyle An attribute in the current theme that contains a reference to a style resource that supplies default
	 *            values for the view. Can be 0 to not look for defaults.
	 * @see android.view.View#View(Context, AttributeSet, int)
	 */
	public VerticalSeekBar(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	protected final void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
		super.onSizeChanged(height, width, oldHeight, oldWidth);
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	@Override
	protected final synchronized void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	protected final void onDraw(@NonNull final Canvas c) {
		c.rotate(ROTATION_ANGLE);
		c.translate(-getHeight(), 0);

		super.onDraw(c);
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	public final void setOnSeekBarChangeListener(final OnSeekBarChangeListener listener) {
		// Do not use super for the listener, as this would not set the fromUser flag properly
		mOnSeekBarChangeListener = listener;
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	public final boolean onTouchEvent(@NonNull final MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setProgressInternally(getMax() - (int) (getMax() * event.getY() / getHeight()), true);
			if (mOnSeekBarChangeListener != null) {
				mOnSeekBarChangeListener.onStartTrackingTouch(this);
			}
			break;

		case MotionEvent.ACTION_MOVE:
			setProgressInternally(getMax() - (int) (getMax() * event.getY() / getHeight()), true);
			break;

		case MotionEvent.ACTION_UP:
			setProgressInternally(getMax() - (int) (getMax() * event.getY() / getHeight()), true);
			if (mOnSeekBarChangeListener != null) {
				mOnSeekBarChangeListener.onStopTrackingTouch(this);
			}
			break;

		case MotionEvent.ACTION_CANCEL:
			if (mOnSeekBarChangeListener != null) {
				mOnSeekBarChangeListener.onStopTrackingTouch(this);
			}
			break;

		default:
			break;
		}

		return true;
	}

	/**
	 * Set the progress by the user. (Unfortunately, Seekbar.setProgressInternally(int, boolean) is not accessible.)
	 *
	 * @param progress the progress.
	 * @param fromUser flag indicating if the change was done by the user.
	 */
	public final void setProgressInternally(final int progress, final boolean fromUser) {
		if (progress != getProgress()) {
			super.setProgress(progress);
			if (mOnSeekBarChangeListener != null) {
				mOnSeekBarChangeListener.onProgressChanged(this, progress, fromUser);
			}
		}
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	public final void setProgress(final int progress) {
		setProgressInternally(progress, false);
	}
}
