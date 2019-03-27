package com.packageai;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Level;

import crosby.binary.osmosis.OsmosisBinaryParser;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import org.openstreetmap.osmosis.osmbinary.file.BlockInputStream;

public class AyalReader implements RunnableSource {

	private Sink sink;
	private InputStream input;
	private OsmosisBinaryParser parser;


	public AyalReader(InputStream input) {
		if (input == null) {
			throw new Error("Null input");
		}
		this.input = input;
		parser = new OsmosisBinaryParser();
	}

	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
		parser.setSink(sink);
	}

	@Override
	public void run() {

		BlockInputStream blockInputStream = null;
		try {
			sink.initialize(Collections.<String, Object>emptyMap());

			blockInputStream = new BlockInputStream(input, parser);
			blockInputStream.process();
			blockInputStream.close();

		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to process PBF stream", e);
		} finally {
			sink.close();
			parser.complete();
			try{
				input.close();
			}catch (IOException e){
				throw new OsmosisRuntimeException("Unable to close input stream.", e);
			}
			if (blockInputStream != null){
				try{
					blockInputStream.close();
				}catch (IOException e){
					throw new OsmosisRuntimeException("Unable to close block input stream.", e);
				}
			}
		}
	}
}
