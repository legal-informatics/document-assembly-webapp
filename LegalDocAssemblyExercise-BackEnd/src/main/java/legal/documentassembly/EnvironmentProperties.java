package legal.documentassembly;

import java.io.FileInputStream;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

public class EnvironmentProperties {

        @Context
        private static ServletContext servletContext;
    
	private static String propertiesFile = "config.properties";

        public static String lib_folder;
	public static String input_folder;
	public static String output_folder;
	public static String temp_folder;

	public static String reasoner_path;
	public static String lrml_rulebase_filename;
	public static String lrml2drdevice_xsl_filename;
	public static String rulebase_filename;
	public static String rdf_filename;
	public static String rdf_namespace_prefix;
	public static String result_filename;
	public static String proof_filename;
	public static String start_reasoner_command;
	public static String clipsdos32_filename;
	public static String clipsdos64_filename;
	public static String clipsdos_filename;

	public static String caf_filename;
	public static String proof2caf_xsl_filename;
	public static String document_facts_filename;
	public static String document_filename;

        public static void initialize(String path) {
		try {
                        path = (path.endsWith("/") || path.endsWith("\\")) ? path : (path+"/");
			Properties properties = new Properties();
			FileInputStream fis = new FileInputStream(path + propertiesFile);
			properties.load(fis);

                        lib_folder = path + "../lib/";
			input_folder = path + properties.getProperty("input_folder");
			output_folder = path + properties.getProperty("output_folder");
			temp_folder = path + properties.getProperty("temp_folder");

			reasoner_path = properties.getProperty("reasoner_path");
			lrml_rulebase_filename = input_folder + properties.getProperty("lrml_rulebase_filename");
			lrml2drdevice_xsl_filename = input_folder + properties.getProperty("lrml2drdevice_xsl_filename");
			rulebase_filename = properties.getProperty("rulebase_filename");
			rdf_filename = properties.getProperty("rdf_filename");
			rdf_namespace_prefix = properties.getProperty("rdf_namespace_prefix");
			result_filename = properties.getProperty("result_filename");
			proof_filename = properties.getProperty("proof_filename");
			start_reasoner_command = properties.getProperty("start_reasoner_command");
			clipsdos32_filename = reasoner_path + properties.getProperty("clipsdos32_filename");
			clipsdos64_filename = reasoner_path + properties.getProperty("clipsdos64_filename");
			clipsdos_filename = reasoner_path + properties.getProperty("clipsdos_filename");

			caf_filename = output_folder + properties.getProperty("caf_filename");
			proof2caf_xsl_filename = input_folder + properties.getProperty("proof2caf_xsl_filename");
			document_facts_filename = temp_folder + properties.getProperty("document_facts_filename");
			document_filename = output_folder + properties.getProperty("document_filename");

			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
