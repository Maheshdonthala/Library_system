package com.yourdomain.librarysystem.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class MongoConfig {

    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    @Primary
    public MongoClient mongoClient() {
        String uri = mongoUri;
        if (uri != null && uri.startsWith("mongodb+srv://")) {
            try {
                uri = convertSrvToStandard(uri);
            } catch (Exception e) {
                // If conversion fails, log and fall back to original URI so startup still proceeds
                logger.warn("Failed to convert mongodb+srv URI to standard form: {}", e.getMessage());
            }
        }
        return MongoClients.create(new ConnectionString(uri));
    }

    // Convert a mongodb+srv://host/... URI into mongodb://host1:port,host2:port/... by using dnsjava
    private String convertSrvToStandard(String srvUri) throws Exception {
        // Example srvUri: mongodb+srv://user:pass@cluster0.xyz.mongodb.net/mydb?retryWrites=true&w=majority

        String withoutScheme = srvUri.substring("mongodb+srv://".length());

        String authPart = null;
        String hostsAndRest;
        if (withoutScheme.contains("@")) {
            int at = withoutScheme.indexOf('@');
            authPart = withoutScheme.substring(0, at);
            hostsAndRest = withoutScheme.substring(at + 1);
        } else {
            hostsAndRest = withoutScheme;
        }

        // split host (the SRV base) from path/query
        String hostOnly;
        String rest = "";
        int slash = hostsAndRest.indexOf('/');
        if (slash >= 0) {
            hostOnly = hostsAndRest.substring(0, slash);
            rest = hostsAndRest.substring(slash); // includes leading /
        } else {
            int q = hostsAndRest.indexOf('?');
            if (q >= 0) {
                hostOnly = hostsAndRest.substring(0, q);
                rest = hostsAndRest.substring(q);
            } else {
                hostOnly = hostsAndRest;
            }
        }

        String srvName = "_mongodb._tcp." + hostOnly;

        Lookup lookup = new Lookup(srvName, Type.SRV);
        Record[] records = lookup.run();
        if (records == null || records.length == 0) {
            throw new IllegalStateException("No SRV records found for " + srvName);
        }

        List<String> hosts = new ArrayList<>();
        for (Record r : records) {
            SRVRecord srv = (SRVRecord) r;
            String target = srv.getTarget().toString();
            // dnsjava target ends with a dot; strip it
            if (target.endsWith(".")) {
                target = target.substring(0, target.length() - 1);
            }
            hosts.add(target + ":" + srv.getPort());
        }

        // TXT records may contain connection options
        Lookup txtLookup = new Lookup(hostOnly, Type.TXT);
        Record[] txtRecords = txtLookup.run();
        StringBuilder options = new StringBuilder();
        if (txtRecords != null) {
            for (Record tr : txtRecords) {
                TXTRecord txt = (TXTRecord) tr;
                @SuppressWarnings("unchecked")
                List<String> strings = txt.getStrings();
                for (String s : strings) {
                    // each s may be like "replicaSet=...&authSource=..."
                    if (options.length() > 0) options.append("&");
                    options.append(s);
                }
            }
        }

        // Build mongodb:// URI
        StringBuilder sb = new StringBuilder();
        sb.append("mongodb://");
        if (authPart != null) {
            sb.append(authPart).append("@");
        }
        sb.append(hosts.stream().collect(Collectors.joining(",")));

        // append database and existing query parameters from original rest
        String dbAndQuery = "";
        if (rest != null && !rest.isEmpty()) {
            // rest starts with / or ?
            if (rest.startsWith("/")) {
                // find ?
                int q = rest.indexOf('?');
                if (q >= 0) {
                    dbAndQuery = rest.substring(0, q); // includes /dbname
                    String qpart = rest.substring(q + 1);
                    if (!qpart.isEmpty()) {
                        if (options.length() > 0) options.append("&");
                        options.append(qpart);
                    }
                } else {
                    dbAndQuery = rest; // only /dbname
                }
            } else if (rest.startsWith("?")) {
                String qpart = rest.substring(1);
                if (!qpart.isEmpty()) {
                    if (options.length() > 0) options.append("&");
                    options.append(qpart);
                }
            }
        }

        if (dbAndQuery != null && !dbAndQuery.isEmpty()) {
            sb.append(dbAndQuery);
        } else {
            sb.append("/");
        }

        // Ensure TLS is enabled for Atlas SRV
        if (options.length() > 0) {
            // make sure tls=true is present
            String opts = options.toString();
            if (!opts.contains("tls=") && !opts.contains("ssl=")) {
                sb.append("?").append("tls=true&").append(opts);
            } else {
                sb.append("?").append(opts);
            }
        } else {
            sb.append("?tls=true");
        }

        String converted = sb.toString();
        logger.info("Converted SRV URI to standard URI: {}", converted);
        return converted;
    }
}
