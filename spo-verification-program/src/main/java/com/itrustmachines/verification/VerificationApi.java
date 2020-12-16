package com.itrustmachines.verification;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileInputStream;

import org.apache.commons.io.*;
import org.apache.commons.cli.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import com.itrustmachines.common.util.HashUtils;

import com.itrustmachines.verification.service.VerifyVerificationProofService;
import com.itrustmachines.verification.util.VerificationProofParser;
import com.itrustmachines.verification.vo.VerificationProof;
import com.itrustmachines.verification.vo.VerifyVerificationProofResult;

import lombok.NonNull;
import lombok.Synchronized;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.rnorth.visibleassertions.VisibleAssertions.*;

@ToString
@Slf4j
public class VerificationApi {

    public static final String INFURA_PROJECT_ID_OPT = "infuraProjectId";
    public static final String PROOF_OPT = "proof";
    public static final String RESULT_OPT = "result";

    private final VerifyVerificationProofService service;
    public static final Gson gson = new Gson();

    private VerificationApi() {
        this.service = new VerifyVerificationProofService();
        log.info("new instance: {}", this);
    }

    private static VerificationApi instance;

    @Synchronized
    public static VerificationApi getInstance() {
        if (VerificationApi.instance == null) {
            VerificationApi.instance = new VerificationApi();
        }
        return VerificationApi.instance;
    }

    public VerifyVerificationProofResult verify(@NonNull final String filePath, final String infuraProjectId) {
        final VerificationProof verificationProof = VerificationProofParser.parse(filePath);
        return verify(verificationProof, infuraProjectId);
    }

    public VerifyVerificationProofResult verifyJsonString(@NonNull final String jsonString,
                                                          final String infuraProjectId) {
        final VerificationProof verificationProof = VerificationProofParser.parseJsonString(jsonString);
        return verify(verificationProof, infuraProjectId);
    }

    public VerifyVerificationProofResult verify(final VerificationProof proof, final String infuraProjectId) {
        log.debug("verify() proof={}", proof);
        VerifyVerificationProofResult result = null;
        if (proof != null) {
            result = service.verify(proof, infuraProjectId);
        }
        return result;
    }

    public static void replaceFileString(String fileName, String old, String new1) throws IOException {
        //String fileName = Settings.getValue("fileDirectory");
        FileInputStream fis = new FileInputStream(fileName);
        String content = IOUtils.toString(fis, StandardCharsets.UTF_8);
        content = content.replace(old, new1);
        FileOutputStream fos = new FileOutputStream(fileName);
        IOUtils.write(content, new FileOutputStream(fileName), StandardCharsets.UTF_8);
        fis.close();
        fos.close();
    }

    public static void main(String[] args) {
        final Options options = new Options();
        final Option filePathOption = Option.builder()
                .argName("filePath")
                .longOpt(PROOF_OPT)
                .hasArg()
                .desc("input verification proof file path (sample/queryByCO.json)")
                .optionalArg(false)
                .required()
                .build();
        final Option resultPathOption = Option.builder()
                .argName("filePath")
                .longOpt(RESULT_OPT)
                .hasArg()
                .desc("output verify result file path (result.json)")
                .optionalArg(false)
                .required()
                .build();
        final Option infuraProjectIdOption = Option.builder()
                .argName("infuraProjectId")
                .longOpt(INFURA_PROJECT_ID_OPT)
                .hasArg()
                .desc("required if env is MAINNET, KOVAN, GOERLI, RINKEBY, ROPSTEN")
                .optionalArg(true)
                .build();
        options.addOption(filePathOption);
        options.addOption(resultPathOption);
        options.addOption(infuraProjectIdOption);
        final Gson gson = new Gson();
        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine line = parser.parse(options, args);

            String infuraProjectId = line.getOptionValue(INFURA_PROJECT_ID_OPT, null);
            log.debug("infuraProjectId={}", infuraProjectId);
            final String filePath = line.getOptionValue(PROOF_OPT);
            log.debug("filePath={}", filePath);
            //replaceFileString(filePath, "\\\\","");

            final String resultPath = line.getOptionValue(RESULT_OPT);
            log.debug("resultPath={}", resultPath);

            final VerificationApi verificationApi = VerificationApi.getInstance();

            final VerifyVerificationProofResult result = verificationApi.verify(filePath, infuraProjectId);
            log.debug("result={}", result);
            if (result.isPass()) {
                try {
                    assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                    assertThat("####### VerifyVerificationProofResult Pass ####### " + filePath, "Pass", is(equalTo("Pass")));
                    assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                } catch (AssertionError expected) {
                }

                //log.debug("isPass=####### Failed ####### \n{}", result.getVerifyReceiptResults());
                log.debug("getProofCount={}", result.getProofCount());
                log.debug("####### VerifyReceiptResults Start ####### " + filePath);

                for (int i = 0; i < result.getVerifyReceiptResults().size(); i++) {

                    String BackVideoValue = "";
                    String FrontVideoValue = "";
                    String NMEAValue = "";

                    //log.debug("cmd={}", result.getVerifyReceiptResults().get(i).getCmd());
                    String cmd = result.getVerifyReceiptResults().get(i).getCmd();
                    //cmd.replace("\\\\", "");

                    //String json = gson.toJson(cmd);
                    //json.replaceAll("\\\\\\", "");
                    //log.debug("Using Gson.toJson() on a raw collection: " + json);
                    JsonParser Jparser = new JsonParser();
                    // JsonArray array = Jparser.parse(cmd).getAsJsonArray();

                    JsonObject jo = (JsonObject) Jparser.parse(cmd);
                    JsonElement je = jo.get("info");
                    //je.toString().replaceAll("\"", "");
                    //log.debug("info={}", je);

                    //JsonObject jo1 = je.getAsJsonObject();
                    JsonObject jo1 = (JsonObject) Jparser.parse(je.getAsString());

                    JsonElement je1 = jo1.get("BackVideo");
                    JsonElement je1_hash = jo1.get("BackVideo_HASH");
                    log.debug("je1={}", je1.getAsString());
                    log.debug("je1_hash={}", je1_hash.getAsString());

                    File currentJavaJarFile = new File(VerificationApi.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                    String currentJavaJarFilePath = currentJavaJarFile.getAbsolutePath();
                    String currentRootDirectoryPath = currentJavaJarFilePath.replace(currentJavaJarFile.getName(), "");

                    File BackVideoFile = new File(je1.getAsString().replaceAll("\"", ""));
                    if (BackVideoFile.canRead()) {
                        BackVideoValue = HashUtils.sha256(BackVideoFile);
                        try {
                            assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                            assertThat("BackVideoValue verify:" + je1.getAsString(), BackVideoValue, is(equalTo(je1_hash.getAsString())));
                            assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                        } catch (AssertionError expected) {
                        }

                    } else {
                        try {
                            assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                            assertThat("####### BackVideo HASH EMPTY ####### " + je1.getAsString(), "HASH EMPTY", is(equalTo("OK")));
                            assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                        } catch (AssertionError expected) {
                        }

                    }

                    JsonElement je2 = jo1.get("FrontVideo");
                    JsonElement je2_hash = jo1.get("FrontVideo_HASH");
                    log.debug("je2={}", je2.getAsString());
                    log.debug("je2_hash={}", je2_hash.getAsString());

                    //File FrontVideoFile = new File(currentRootDirectoryPath + File.separator + je2.getAsString().replaceAll("\"", ""));
                    File FrontVideoFile = new File(je2.getAsString().replaceAll("\"", ""));
                    if (FrontVideoFile.canRead()) {
                        FrontVideoValue = HashUtils.sha256(FrontVideoFile);
                        try {
                            assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                            assertThat("FrontVideoValue verify: " + je2.getAsString(), FrontVideoValue, is(equalTo(je2_hash.getAsString())));
                            assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                        } catch (AssertionError expected) {
                        }

                    } else {
                        try {
                            assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                            assertThat("####### FrontVideo HASH EMPTY #######: " + je2.getAsString(), "HASH EMPTY", is(equalTo("OK")));
                            assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                        } catch (AssertionError expected) {
                        }

                    }

                    JsonElement je3 = jo1.get("NMEA");
                    JsonElement je3_hash = jo1.get("NMEA_HASH");
                    log.debug("je3={}", je3.getAsString());
                    log.debug("je3_hash={}", je3_hash.getAsString());

                    File NMEAValueFile = new File(je3.getAsString().replaceAll("\"", ""));
                    if (NMEAValueFile.canRead()) {
                        NMEAValue = HashUtils.sha256(NMEAValueFile);
                        try {
                            assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                            assertThat("NMEAValue verify: " + je3.getAsString(), NMEAValue, is(equalTo(je3_hash.getAsString())));
                            assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                        } catch (AssertionError expected) {
                        }

                    } else {
                        try {
                            assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                            assertThat("####### NMEA HASH EMPTY #######: " + je3.getAsString(), "HASH EMPTY", is(equalTo("OK")));
                            assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                        } catch (AssertionError expected) {
                        }

                    }
                }

                log.debug("####### VerifyReceiptResults end ####### " + filePath);

            } else {

                try {
                    assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                    assertThat("####### VerifyVerificationProofResult Pass ####### " + filePath, "Fail", is(equalTo("Pass")));
                    assertThat("#######                                    ####### " , "Pass", is(equalTo("Pass")));
                } catch (AssertionError expected) {
                }
            }

            final FileOutputStream fileOutputStream = new FileOutputStream(resultPath);
            final Writer out = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
            out.write(new Gson().toJson(result));
            out.close();
        } catch (ParseException e) {
            log.error("error", e);
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("verification-api", options);
        } catch (Exception e) {
            log.error("verifiy error", e);
        }
    }

}