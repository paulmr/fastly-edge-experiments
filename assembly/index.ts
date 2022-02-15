//! Default Compute@Edge template program.
import { JSONEncoder } from "assemblyscript-json";
import { decodeURIComponent } from "as-uri-encode-decode"
import { Request, Response, Headers, URL, Fastly } from "@fastly/as-compute";

const dictionaryName = "metering_test_members_ex";
const debugLog = Fastly.getLogEndpoint("debug")
const dictionary = new Fastly.Dictionary(dictionaryName);

function static_lookup(id: string): string {
    return testGroup.includes(id) ? 'Test' : 'NotInTest';
}

function dictionary_lookup(id: string): string {
    if(dictionary.contains(id)) {
        let res = dictionary.get(id);
        return res ? res : 'Test';
    } else {
        debugLog.log("missing (" + id + ")");
        return 'NotInTest';
    }
}

function extractParams(body: string): Map<string, string[]> {
    let res = new Map<string, string[]>();
    let params = body.split("&");
    for(let i = 0; i < params.length; i++) {
        let keyval = params[i].split("=", 2);
        let key = keyval[0];
        let val = decodeURIComponent(keyval[1]);
        if(res.has(keyval[0])) {
            res.get(keyval[0]).push(keyval[1]);
        } else {
            res.set(key, [ val ]);
        }
    };
    return res;
}

function main(req: Request): Response {
    // Filter requests that have unexpected methods.
    if (req.method != "POST") {
        return new Response(String.UTF8.encode("This method is not allowed"), {
            status: 405,
            headers: null,
            url: null
        });
    }

    let url = new URL(req.url);

    let params = extractParams(req.text());
    let id = params.get("browserId")[0];

    let headers = new Headers();
    headers.set('Content-Type', 'application/json; charset=utf-8');

    let res = new JSONEncoder();

    res.setString("browserId", id);
    res.setString("testGroup", dictionary_lookup(id));

    // if(command == "static") {
    //     res.setString("group", static_lookup(id));
    // }

    return new Response(String.UTF8.encode("{" + res.toString() + "}"), {
        status: 200,
        headers,
        url: null
    });
}

// Get the request from the client.
let req = Fastly.getClientRequest();

// Pass the request to the main request handler function.
let resp = main(req);

// Send the response back to the client.
Fastly.respondWith(resp);
