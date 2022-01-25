
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

    // If request is to the `/` path...
    if (url.pathname.startsWith("/id/")) {
        let id = url.pathname.substring(4)

        let headers = new Headers();
        headers.set('Content-Type', 'application/json; charset=utf-8');

        let res = new JSONEncoder();
        res.setString('id', id);

        if(testGroup.includes(id)) {
            res.setString('group', 'test');
        } else {
            res.setString('group', 'none');
        }
        return new Response(String.UTF8.encode("{" + res.toString() + "}"), {
            status: 200,
            headers,
            url: null
        });
    }

    // Catch all other requests and return a 404.
    return new Response(String.UTF8.encode("The page you requested could not be found"), {
        status: 404,
        headers: null,
        url: null
    });
}

// Get the request from the client.
let req = Fastly.getClientRequest();

// Pass the request to the main request handler function.
let resp = main(req);

// Send the response back to the client.
Fastly.respondWith(resp);
