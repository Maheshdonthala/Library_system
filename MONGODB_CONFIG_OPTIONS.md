# MongoDB Atlas Configuration Options

## Current Configuration Issue
The error indicates authentication failure with MongoDB Atlas. Here are several solutions to try:

## Option 1: Use Admin Authentication Source (Current Fix)
```
mongodb+srv://guest:guest123@cluster0.9glzuqu.mongodb.net/librarydb?authSource=admin&retryWrites=true&w=majority
```

## Option 2: Try Alternative Database Names
Sometimes the database name needs to be different:
```
mongodb+srv://guest:guest123@cluster0.9glzuqu.mongodb.net/test?authSource=admin&retryWrites=true&w=majority
mongodb+srv://guest:guest123@cluster0.9glzuqu.mongodb.net/?authSource=admin&retryWrites=true&w=majority
```

## Option 3: Use Different Credentials
If guest/guest123 doesn't work, try creating new credentials in MongoDB Atlas:
1. Go to MongoDB Atlas Dashboard
2. Navigate to Database Access
3. Create a new user with read/write permissions
4. Use those credentials instead

## Option 4: Check Atlas Cluster Status
- Ensure the cluster is running (not paused)
- Check IP whitelist (should allow 0.0.0.0/0 for Render)
- Verify the cluster is in the correct region

## Option 5: Use Free Public MongoDB (Fallback)
If Atlas continues to fail, we can use a public test database:
```
mongodb+srv://readonly:readonly@cluster0.e8nce.mongodb.net/sample_mflix?retryWrites=true&w=majority
```

## Environment Variables for Render
Set in Render Dashboard under Environment:
- Key: MONGODB_URI
- Value: [One of the above URIs]

## Testing Locally
To test the connection locally:
```bash
mvn spring-boot:run
```
Check logs for MongoDB connection status.