
//! Default Compute@Edge template program.
import { JSONEncoder } from "assemblyscript-json";
import { Request, Response, Headers, URL, Fastly } from "@fastly/as-compute";

// The entry point for your application.
//
// Use this function to define your main request handling logic. It could be
// used to route based on the request properties (such as method or path), send
// the request to a backend, make completely new requests, and/or generate
// synthetic responses.

const testGroup: Array<String> = Inliner.inlineFileAsString(
  "../data.csv"
).split("\n");

const dictionary = new Fastly.Dictionary("metering_test_members");

const debugLog = Fastly.getLogEndpoint("debug")

function log(s: string): void {
    debugLog.log(s)
}

function static_lookup(id: string): string {
    return testGroup.includes(id) ? 'test-1' : 'none';
}

function dictionary_lookup(id: string): string {
    return dictionary.contains(id) ? 'test-1' : 'none';
}

function main(req: Request): Response {
    // Filter requests that have unexpected methods.
    if (!["HEAD", "GET"].includes(req.method)) {
        return new Response(String.UTF8.encode("This method is not allowed"), {
            status: 405,
            headers: null,
            url: null
        });
    }

    let url = new URL(req.url);

    let path = url.pathname.split("/")
    let commands = path[1].split(",")
    let id = path.slice(2).join("/")

    let headers = new Headers();
    headers.set('Content-Type', 'application/json; charset=utf-8');

    let res = new JSONEncoder();

    for(let i = 0; i < commands.length; i++) {
        let command = commands[i];
        res.pushObject(command);
        res.setString("id", id);

        if(command == "static") {
            res.setString("group", static_lookup(id));
        }

        if(command == "dic") {
            res.setString("group", dictionary_lookup(id));
        }

        res.popObject();
    }

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
