# Introduction to schema workshop



<h3>Copied from http://blog.getprismatic.com/schema-0-2-0-back-with-clojurescript-data-coercion/</h3>
<h3 id="coercion">Coercion</h3>

<p>Runtime schema validation is a valuable tool for pinpointing mismatches between your expectations and your real data.  Sometimes, this assurance that your data is correct is all that's needed.  But in other cases, mismatches are actually <em>anticipated</em>, and rather than throw up your hands, you'd like to actually <em>fix</em> the data and get on with the task at hand.</p>

<p>For example, our backend provides a JSON API for use by iOS and web clients.  One of the methods allow a user to post a comment on a story. The request body might look something like this:</p>

<pre><code>{"parent-comment-id": 2128123123, "text": "This is awesome!", "share-services": ["twitter" "facebook"]}
</code></pre>

<p>On the backend (with the appropriate <a href="https://github.com/ring-clojure/ring">Ring</a> middleware) this will show up as the Clojure data structure <code>+bad-request+</code> above.  This is almost, but not quite, what we want: an instance of the <code>CommentRequest</code> schema.  To resolve the inconsistencies, we can write some fiddly code for traversing and updating the request:</p>

<script src="https://gist.github.com/w01fe/8248664.js"></script>

<p>This works but writing such code gets old fast, especially when the same data types show up (possibly deeply nested) across many request types.  It is especially frustrating since this seems to be just restating the <code>CommentRequest</code> schema in code: if <code>parent-comment-id</code> is present, it must be a long; and <code>share-services</code> must be a list of service keywords.  </p>

<p>In fact, this is the key idea motivating schema transformations. In cases like these, the schema already contains the information needed to coerce the data into a format that validates:</p>

<script src="https://gist.github.com/w01fe/8248688.js"></script>

<p>Here, the <code>coercer</code> makes a single pass over the request, simultaneously coercing values and validating that the final request is a legal <code>CommentRequest</code>.  The coercions are provided by <code>json-coercion-matcher</code>, which has some useful defaults for coercing from JSON, such as:</p>

<ul>
<li>Numbers should be coerced to the expected type, if this can be done without losing precision</li>
<li>When a Keyword is expected, a String can be coerced to the correct type by calling <code>keyword</code> on it</li>
</ul>

<p>There's nothing special about <code>json-coercion-matcher</code> though; it's just as easy to make your own schema-specific transformations to do even more.  For example, many of our JSON API responses include <code>Comment</code> objects.  Our backend data model includes a <code>Comment</code> record with a <code>user-id</code> field, but for presentation to the client, a <code>Comment</code> must be expanded out into a more complex (potentially API-version-dependant) <code>ClientComment</code> that transforms the <code>user-id</code> into a full-fledged <code>ClientUser</code> with a username and profile image.  Accomplishing this previously required injecting resources to clientize a <code>Comment</code> (username lookup, API version, etc.) into every function that generated a response containing a <code>Comment</code>.</p>

<p>With schema transformations, we can just create a coercer for <code>ClientComment</code>:</p>

<script src="https://gist.github.com/w01fe/8249101.js"></script>

<p>and apply it when validating API responses, so that all API methods can return backend <code>Comment</code> objects (at arbitrary nesting levels), and clientization happens automatically. </p>

<p>In our production API service, we annotate all of our API methods with schema metadata, provide a pluggable multimethod for defining coercions, and all of this input and output coercion and validation happens automatically with zero user-level code.  Stay tuned for an open-source release showcasing this in the near future. </p>

