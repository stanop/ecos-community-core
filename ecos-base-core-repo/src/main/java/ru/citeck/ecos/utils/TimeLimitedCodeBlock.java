/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class TimeLimitedCodeBlock<T> {
	
	private long timeoutMs;
	
	public TimeLimitedCodeBlock(long timeoutMs) {
		this.timeoutMs = timeoutMs;
	}
	
	protected abstract T codeBlock() throws Throwable;
	
	public T run() throws Throwable {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future<T> future = executor.submit(new Callable<T>() {
			public T call() throws Exception {
				try {
					return codeBlock();
				} catch (Exception e) {
					throw e;
				} catch (Throwable e) {
					// we shouldn't normally get here
					throw new Exception(e);
				}
			}
		});
		executor.shutdown(); // This does not cancel the already-scheduled task.
		try {
			return future.get(timeoutMs, TimeUnit.MILLISECONDS);
		} finally {
			if (!executor.isTerminated())
			    executor.shutdownNow(); // If you want to stop the code that hasn't finished.
		}
	}

}
