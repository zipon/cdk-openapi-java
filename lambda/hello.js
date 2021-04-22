exports.handler = async function(event) {
  console.log("request:", JSON.stringify(event, undefined, 2));
  return {
    statusCode: 200,
    headers: { "Content-Type": "text/plain" },
    body: `Hello and congratulation.\nYou have deployed and executed a RestAPI from CDK with OpenAPI definition You've hit ${event.path}\n`
  };
};
