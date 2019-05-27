package com.serenegiant.media;
/*
 * libcommon
 * utility/helper classes for myself
 *
 * Copyright (c) 2014-2019 saki t_saki@serenegiant.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;

@SuppressLint("NewApi")
public class MediaAVRecorder extends AbstractMediaAVRecorder {
//	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = MediaAVRecorder.class.getSimpleName();

	@NonNull
	private final IMuxerFactory mMuxerFactory;

	/**
	 * コンストラクタ
	 * @param context
	 * @param callback
	 * @param ext 出力ファイルの拡張子
	 * @param saveTreeId
	 * @throws IOException
	 */
	public MediaAVRecorder(@NonNull final Context context,
		@Nullable final RecorderCallback callback,
		final String ext, final int saveTreeId)
			throws IOException {

		this(context, callback, ext, saveTreeId, null);
	}

	/**
	 * コンストラクタ
	 * @param context
	 * @param callback
	 * @param ext 出力ファイルの拡張子
	 * @param saveTreeId
	 * @param factory
	 * @throws IOException
	 */
	public MediaAVRecorder(@NonNull final Context context,
		@Nullable final RecorderCallback callback,
		final String ext, final int saveTreeId,
		@Nullable final IMuxerFactory factory) throws IOException {

		super(context, callback, null, ext, saveTreeId);
		mMuxerFactory = factory != null ? factory : new DefaultFactory();
	}

	/**
	 * コンストラクタ
	 * @param context
	 * @param callback
	 * @param prefix
	 * @param _ext
	 * @param saveTreeId
	 * @throws IOException
	 */
	public MediaAVRecorder(@NonNull final Context context,
		@Nullable final RecorderCallback callback,
		final String prefix, final String _ext, final int saveTreeId)
			throws IOException {

		this(context, callback, prefix, _ext, saveTreeId, null);
	}

	/**
	 * コンストラクタ
	 * @param context
	 * @param callback
	 * @param prefix
	 * @param _ext
	 * @param saveTreeId
	 * @param factory
	 * @throws IOException
	 */
	public MediaAVRecorder(@NonNull final Context context,
		@Nullable final RecorderCallback callback,
		final String prefix, final String _ext, final int saveTreeId,
		@Nullable final IMuxerFactory factory) throws IOException {

		super(context, callback, prefix, _ext, saveTreeId);
		mMuxerFactory = factory != null ? factory : new DefaultFactory();
	}

	/**
	 * コンストラクタ
	 * @param context
	 * @param callback
	 * @param saveTreeId
	 * @param dirs savedTreeIdが示すディレクトリからの相対ディレクトリパス, nullならsavedTreeIdが示すディレクトリ
	 * @param fileName
	 * @throws IOException
	 */
	public MediaAVRecorder(@NonNull final Context context,
		@Nullable final RecorderCallback callback,
		final int saveTreeId, @Nullable final String dirs, @NonNull final String fileName)
			throws IOException {

		this(context, callback, saveTreeId, dirs, fileName, null);
	}

	/**
	 * コンストラクタ
	 * @param context
	 * @param callback
	 * @param saveTreeId
	 * @param dirs savedTreeIdが示すディレクトリからの相対ディレクトリパス, nullならsavedTreeIdが示すディレクトリ
	 * @param fileName
	 * @param factory
	 * @throws IOException
	 */
	public MediaAVRecorder(@NonNull final Context context,
		@Nullable final RecorderCallback callback,
		final int saveTreeId, @Nullable final String dirs, @NonNull final String fileName,
		@Nullable final IMuxerFactory factory) throws IOException {

		super(context, callback, saveTreeId, dirs, fileName);
		mMuxerFactory = factory != null ? factory : new DefaultFactory();
	}

	/**
	 * コンストラクタ
	 * @param context
	 * @param callback
	 * @param output
	 * @throws IOException
	 */
	public MediaAVRecorder(@NonNull final Context context,
		@Nullable final RecorderCallback callback,
		@NonNull final DocumentFile output) throws IOException {

		this(context, callback, output, null);
	}

	/**
	 * コンストラクタ
	 * @param context
	 * @param callback
	 * @param output
	 * @param factory
	 * @throws IOException
	 */
	public MediaAVRecorder(@NonNull final Context context,
		@Nullable final RecorderCallback callback,
		@NonNull final DocumentFile output,
		@Nullable final IMuxerFactory factory) throws IOException {

		super(context, callback, output);
		mMuxerFactory = factory != null ? factory : new DefaultFactory();
	}

	/**
	 * コンストラクタ
	 * @param context
	 * @param callback
	 * @param outputPath
	 * @throws IOException
	 */
	public MediaAVRecorder(@NonNull final Context context,
		@Nullable final RecorderCallback callback,
		@NonNull final String outputPath)
			throws IOException {

		this(context, callback, outputPath, null);
	}

	/**
	 * コンストラクタ
	 * @param context
	 * @param callback
	 * @param outputPath
	 * @throws IOException
	 */
	public MediaAVRecorder(@NonNull final Context context,
		@Nullable final RecorderCallback callback,
		@NonNull final String outputPath,
		@Nullable final IMuxerFactory factory) throws IOException {

		super(context, callback, outputPath);
		mMuxerFactory = factory != null ? factory : new DefaultFactory();
	}

	@Override
	protected void setupMuxer(final int fd) throws IOException {
		setMuxer(mMuxerFactory.createMuxer(VideoConfig.sUseMediaMuxer, fd));
	}

	@Override
	protected void setupMuxer(@NonNull final String output) throws IOException {
		setMuxer(mMuxerFactory.createMuxer(VideoConfig.sUseMediaMuxer, output));
	}

	@Override
	protected void setupMuxer(
		@NonNull final Context context,
		@NonNull final DocumentFile output) throws IOException {

		setMuxer(mMuxerFactory.createMuxer(context, VideoConfig.sUseMediaMuxer, output));
	}
	
}
