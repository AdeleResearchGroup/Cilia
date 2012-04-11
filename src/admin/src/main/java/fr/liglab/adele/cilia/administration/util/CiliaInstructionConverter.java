package fr.liglab.adele.cilia.administration.util;

import fr.liglab.adele.cilia.Data;

public class CiliaInstructionConverter {

	public Data getDataFromInstruction(String line2) {
		if (line2.startsWith("create")) {
			return create(line2);
		} else if (line2.startsWith("remove")) {
			return remove(line2);
		} else if (line2.startsWith("start")) {
			return start(line2);
		} else if (line2.startsWith("show") || line2.length() < 1) {
			return show(line2);
		} else if (line2.startsWith("stop")) {
			return stop(line2);
		} else if (line2.startsWith("modify")) {
			return modify(line2);
		} else if (line2.startsWith("load") || line2.startsWith("unload")) {
			return load(line2);
		} else if (line2.startsWith("replace")) {
			return replace(line2);
		} else if (line2.startsWith("copy")) {
			return copy(line2);
		}
		return null;
	}

	/**
	 * Prepare a Cilia Data with the line used in the cilia-command. This data
	 * is send to the mediation chain.
	 * 
	 * @param line2
	 *            The parameters in the command.
	 */
	public Data create(String line2) {
		String line3 = line2.substring("create".length()).trim();
		Data data = new Data("create");
		if (line3.startsWith("chain")) {
			data.setProperty("element", "chain");
			data.setProperty("id", ParserUtils.getParameter("id", line3));
		} else if (line3.startsWith("mediator")) {
			data.setProperty("element", "mediator");
			data.setProperty("chain", ParserUtils.getParameter("chain", line3));
			data.setProperty("type", ParserUtils.getParameter("type", line3));
			data.setProperty("id", ParserUtils.getParameter("id", line3));
		} else if (line3.startsWith("adapter")) {
			data.setProperty("element", "adapter");
			data.setProperty("chain", ParserUtils.getParameter("chain", line3));
			data.setProperty("type", ParserUtils.getParameter("type", line3));
			data.setProperty("id", ParserUtils.getParameter("id", line3));
		} else if (line3.startsWith("binding")) {
			data.setProperty("element", "binding");
			data.setProperty("chain", ParserUtils.getParameter("chain", line3));
			data.setProperty("from", ParserUtils.getParameter("from", line3));
			data.setProperty("to", ParserUtils.getParameter("to", line3));
		}
		return data;
	}

	/**
	 * Prepare a Cilia Data with the line used in the cilia-command. This data
	 * is send to the mediation chain.
	 * 
	 * @param line2
	 *            The parameters in the command.
	 */
	public Data replace(String line2) {
		String line3 = line2.substring("replace".length()).trim();
		Data data = new Data("replace");
		data.setProperty("element", "mediator");
		data.setProperty("chain", ParserUtils.getParameter("chain", line3));
		data.setProperty("id", ParserUtils.getParameter("id", line3));
		data.setProperty("by", ParserUtils.getParameter("by", line3));
		return data;
	}

	/**
	 * Prepare a Cilia Data with the line used in the cilia-command. This data
	 * is send to the mediation chain.
	 * 
	 * @param line2
	 *            The parameters in the command.
	 */
	public Data copy(String line2) {
		String line3 = line2.substring("copy".length()).trim();
		Data data = new Data("copy");
		data.setProperty("element", "mediator");
		data.setProperty("chain", ParserUtils.getParameter("chain", line3));
		data.setProperty("from", ParserUtils.getParameter("from", line3));
		data.setProperty("to", ParserUtils.getParameter("to", line3));
		return data;
	}
	
	/**
	 * Prepare a Cilia Data with the line used in the cilia-command. This data
	 * is send to the mediation chain.
	 * 
	 * @param line2
	 *            The parameters in the command.
	 */
	public Data remove(String line2) {
		String line3 = line2.substring("remove".length()).trim();
		Data data = new Data("remove");
		if (line3.startsWith("chain")) {
			data.setProperty("element", "chain");
			data.setProperty("id", ParserUtils.getParameter("id", line3));
		} else if (line3.startsWith("mediator")) {
			data.setProperty("element", "mediator");
			data.setProperty("chain", ParserUtils.getParameter("chain", line3));
			data.setProperty("id", ParserUtils.getParameter("id", line3));
		} else if (line3.startsWith("adapter")) {
			data.setProperty("element", "adapter");
			data.setProperty("chain", ParserUtils.getParameter("chain", line3));
			data.setProperty("id", ParserUtils.getParameter("id", line3));
		} else if (line3.startsWith("binding")) {
			data.setProperty("element", "binding");
			data.setProperty("chain", ParserUtils.getParameter("chain", line3));
			data.setProperty("from", ParserUtils.getParameter("from", line3));
			data.setProperty("to", ParserUtils.getParameter("to", line3));
		}
		return data;
	}

	/**
	 * Prepare a Cilia Data with the line used in the cilia-command. This data
	 * is send to the mediation chain.
	 * 
	 * @param line2
	 *            The parameters in the command.
	 */
	public Data start(String line2) {
		String line3 = line2.substring("start".length()).trim();
		Data data = new Data("start");
		if (line3.startsWith("chain")) {
			data.setProperty("element", "chain");
			data.setProperty("id", ParserUtils.getParameter("id", line3));
		}
		return data;
	}

	/**
	 * Prepare a Cilia Data with the line used in the cilia-command. This data
	 * is send to the mediation chain.
	 * 
	 * @param line2
	 *            The parameters in the command.
	 */
	public Data stop(String line2) {
		String line3 = line2.substring("stop".length()).trim();
		Data data = new Data("stop");
		if (line3.startsWith("chain")) {
			data.setProperty("element", "chain");
			data.setProperty("id", ParserUtils.getParameter("id", line3));
		}
		return data;
	}

	/**
	 * Prepare a Cilia Data with the line used in the cilia-command. This data
	 * is send to the mediation chain.
	 * 
	 * @param line2
	 *            The parameters in the command.
	 */
	public Data show(String line2) {
		Data data = new Data("show");
		if (line2 != null && line2.length() > "show".length()) {
			String line3 = line2.trim().substring("show".length()).trim();
			if (line3.startsWith("chain")) {
				data.setProperty("element", "chain");
				data.setProperty("id", ParserUtils.getParameter("id", line3));
			}
			if (line3.startsWith("mediator")) {
				data.setProperty("element", "mediator");
				data.setProperty("id", ParserUtils.getParameter("id", line3));
				data.setProperty("chain", ParserUtils.getParameter("chain", line3));
			}
			if (line3.startsWith("adapter")) {
				data.setProperty("element", "adapter");
				data.setProperty("id", ParserUtils.getParameter("id", line3));
				data.setProperty("chain", ParserUtils.getParameter("chain", line3));
			}
		}
		return data;
	}

	/**
	 * Prepare a Cilia Data with the line used in the cilia-command. This data
	 * is send to the mediation chain.
	 * 
	 * @param line2
	 *            The parameters in the command.
	 */
	private Data modify(String line2) {
		String line3 = line2.substring("modify".length()).trim();
		Data data = new Data("modify");
		if (line3.startsWith("chain")) {
			data.setProperty("element", "chain");
			data.setProperty("id", ParserUtils.getParameter("id", line3));
		} else if (line3.startsWith("mediator")) {
			data.setProperty("element", "mediator");
			data.setProperty("chain", ParserUtils.getParameter("chain", line3));
			data.setProperty("id", ParserUtils.getParameter("id", line3));
		} else if (line3.startsWith("adapter")) {
			data.setProperty("element", "adapter");
			data.setProperty("chain", ParserUtils.getParameter("chain", line3));
			data.setProperty("id", ParserUtils.getParameter("id", line3));
		} else if (line3.startsWith("binding")) {
			data.setProperty("element", "binding");
			data.setProperty("chain", ParserUtils.getParameter("chain", line3));
			data.setProperty("from", ParserUtils.getParameter("from", line3));
			data.setProperty("to", ParserUtils.getParameter("to", line3));
		}
		data.setProperty("name", ParserUtils.getParameter("property", line3));
		String type = ParserUtils.getParameter("type", line3);
		String valueAsString = ParserUtils.getParameter("value", line3);
		Object value = ParserUtils.getProperty(valueAsString, type);
		data.setProperty("value", value);
		return data;
	}

	/**
	 * Prepare a Cilia Data with the line used in the cilia-command. This data
	 * is send to the mediation chain.
	 * 
	 * @param line2
	 *            The parameters in the command.
	 */
	private Data load(String line2) {
		Data data = new Data("load");
		if (line2.startsWith("load")) {
			data.setProperty("element", "load");
			data.setProperty("url", ParserUtils.getParameter("file", line2));
		} else if (line2.startsWith("unload")) {
			data.setProperty("element", "unload");
			data.setProperty("url", ParserUtils.getParameter("file", line2));
		}
		return data;
	}

}
